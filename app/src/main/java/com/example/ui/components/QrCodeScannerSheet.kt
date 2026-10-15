package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.*
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.util.QrCodeAnalyzer
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeScannerSheet(
    onPayScannedInvoice: (
        title: String,
        merchantName: String,
        amountInSource: Double,
        currency: AppCurrency,
        gateway: PaymentGateway,
        category: ExpenseCategory,
        taxRegion: TaxRegion
    ) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var scannedData by remember { mutableStateOf<ScannedQrData?>(null) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var cameraControlRef by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

    // Laser scan line animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserProgress"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Scan & Pay QR",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "CameraX Instant Invoice & Merchant Scanner",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (scannedData != null) {
                // Checkout confirmation card for scanned QR
                ScannedPaymentConfirmation(
                    data = scannedData!!,
                    onConfirm = { gateway ->
                        val item = scannedData!!
                        onPayScannedInvoice(
                            item.description,
                            item.merchantOrRecipient,
                            item.amountInSource,
                            item.currency,
                            gateway,
                            ExpenseCategory.VENDOR_PAYMENT,
                            item.taxRegion
                        )
                        onDismiss()
                    },
                    onRescan = { scannedData = null }
                )
            } else {
                // Live CameraX Scanner Viewfinder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                val cameraExecutor = Executors.newSingleThreadExecutor()

                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also { analysis ->
                                            analysis.setAnalyzer(
                                                cameraExecutor,
                                                QrCodeAnalyzer { resultText ->
                                                    scannedData = QrCodeParser.parse(resultText)
                                                }
                                            )
                                        }

                                    val cameraSelector = if (useFrontCamera) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }

                                    try {
                                        cameraProvider.unbindAll()
                                        val camera: Camera = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            imageAnalysis
                                        )
                                        cameraControlRef = camera.cameraControl
                                    } catch (_: Exception) {}
                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // High-tech Reticle and animated laser overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val boxSize = size.width * 0.65f
                            val left = (size.width - boxSize) / 2f
                            val top = (size.height - boxSize) / 2f

                            // Dark translucent mask outside viewfinder
                            drawRect(
                                color = Color.Black.copy(alpha = 0.55f),
                                size = size
                            )

                            // Clear cutout for scanner
                            drawRoundRect(
                                color = Color.Transparent,
                                topLeft = Offset(left, top),
                                size = androidx.compose.ui.geometry.Size(boxSize, boxSize),
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                blendMode = BlendMode.Clear
                            )

                            // Corner accents
                            val cornerLen = 28.dp.toPx()
                            val strokeW = 4.dp.toPx()
                            val cornerColor = Color(0xFF10B981)

                            // Top-left
                            drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeW)
                            drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), strokeW)
                            // Top-right
                            drawLine(cornerColor, Offset(left + boxSize, top), Offset(left + boxSize - cornerLen, top), strokeW)
                            drawLine(cornerColor, Offset(left + boxSize, top), Offset(left + boxSize, top + cornerLen), strokeW)
                            // Bottom-left
                            drawLine(cornerColor, Offset(left, top + boxSize), Offset(left + cornerLen, top + boxSize), strokeW)
                            drawLine(cornerColor, Offset(left, top + boxSize), Offset(left, top + boxSize - cornerLen), strokeW)
                            // Bottom-right
                            drawLine(cornerColor, Offset(left + boxSize, top + boxSize), Offset(left + boxSize - cornerLen, top + boxSize), strokeW)
                            drawLine(cornerColor, Offset(left + boxSize, top + boxSize), Offset(left + boxSize, top + boxSize - cornerLen), strokeW)

                            // Laser beam sweep
                            val laserY = top + (boxSize * laserProgress)
                            drawLine(
                                color = Color(0xFF00E5FF),
                                start = Offset(left + 6.dp.toPx(), laserY),
                                end = Offset(left + boxSize - 6.dp.toPx(), laserY),
                                strokeWidth = 2.5.dp.toPx()
                            )
                        }

                        // Top control overlay: Torch and Camera Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    isTorchEnabled = !isTorchEnabled
                                    cameraControlRef?.enableTorch(isTorchEnabled)
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Flashlight",
                                    tint = if (isTorchEnabled) CyanAccent else Color.White
                                )
                            }

                            IconButton(
                                onClick = { useFrontCamera = !useFrontCamera },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Flip Camera",
                                    tint = Color.White
                                )
                            }
                        }

                        // Bottom align instruction
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Point camera at invoice QR or merchant code",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // Permission Denied View
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Camera Permission Required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "A pay needs camera access to scan QR codes and invoice barcodes.",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Text("Grant Camera Access", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Emulator / Demo Quick Test Invoices Carousel
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Or tap a sample merchant code to test instantly:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SampleQrChip("Apex Cloud (₹2,85,000)") {
                            scannedData = QrCodeParser.parse("""{"invoiceId":"INV-2026-089","recipient":"Apex Cloud Systems Inc.","amount":3372.78,"currency":"USD","taxRegion":"US_CA"}""")
                        }
                        SampleQrChip("Instant UPI (₹1,450)") {
                            scannedData = QrCodeParser.parse("upi://pay?pa=starbucks@icici&pn=Starbucks%20Coffee&am=1450.00&cu=INR")
                        }
                        SampleQrChip("Nordic Tech (€1,589)") {
                            scannedData = QrCodeParser.parse("""{"invoiceId":"INV-2026-092","recipient":"Nordic FinTech GmbH","amount":1589.91,"currency":"EUR","taxRegion":"EU_DE"}""")
                        }
                        SampleQrChip("Dubai Retainer (13,913 AED)") {
                            scannedData = QrCodeParser.parse("""{"invoiceId":"INV-2026-093","recipient":"Emirates Digital Ventures","amount":13913.04,"currency":"AED","taxRegion":"UAE_05"}""")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleQrChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.testTag("sample_qr_chip_${label.take(8)}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScannedPaymentConfirmation(
    data: ScannedQrData,
    onConfirm: (PaymentGateway) -> Unit,
    onRescan: () -> Unit
) {
    var selectedGateway by remember { mutableStateOf(PaymentGateway.UPI_INSTANT) }
    var gatewayDropdown by remember { mutableStateOf(false) }

    val amountInInr = CurrencyFormatter.convertToInr(data.amountInSource, data.currency)
    val taxCalc = TaxCalculator.calculate(amountInInr, data.taxRegion)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Invoice / Merchant Verified",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = onRescan) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-scan", fontSize = 12.sp, color = CyanAccent)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Merchant / Payer
            Text(
                text = data.merchantOrRecipient,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            data.invoiceRefId?.let { ref ->
                Text(
                    text = "Ref: $ref • ${data.description}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${data.currency.symbol}${String.format("%.2f", data.amountInSource)} (${data.currency.code})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (data.currency != AppCurrency.INR) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Base In Rupee:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyFormatter.formatInr(amountInInr), fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${data.taxRegion.taxLabel} (${data.taxRegion.defaultRatePercent}%):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(CurrencyFormatter.formatInr(taxCalc.taxAmount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount (INR):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = CurrencyFormatter.formatInr(taxCalc.totalAmount),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = EmeraldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Choose Payment Gateway
            ExposedDropdownMenuBox(
                expanded = gatewayDropdown,
                onExpandedChange = { gatewayDropdown = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedGateway.title,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Route") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gatewayDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = gatewayDropdown,
                    onDismissRequest = { gatewayDropdown = false }
                ) {
                    PaymentGateway.entries.forEach { gw ->
                        DropdownMenuItem(
                            text = { Text(gw.title) },
                            onClick = {
                                selectedGateway = gw
                                gatewayDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { onConfirm(selectedGateway) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_scanned_payment_button")
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Authorize & Pay ${CurrencyFormatter.formatInr(taxCalc.totalAmount)}", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
