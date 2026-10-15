package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    displayCurrency: AppCurrency,
    totalVolumeInr: Double,
    totalTaxInr: Double,
    transactions: List<Transaction>,
    onSelectTransaction: (Transaction) -> Unit,
    onOpenNewPayment: () -> Unit,
    onOpenNewInvoice: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenBudgetCalc: () -> Unit,
    onOpenScanQr: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Balance / KPI Banner
        item {
            HeroBalanceCard(
                displayCurrency = displayCurrency,
                totalVolumeInr = totalVolumeInr,
                totalTaxInr = totalTaxInr,
                onOpenNewPayment = onOpenNewPayment,
                onOpenConverter = onOpenConverter
            )
        }

        // Quick Actions Row
        item {
            QuickActionsRow(
                onOpenNewPayment = onOpenNewPayment,
                onOpenNewInvoice = onOpenNewInvoice,
                onOpenConverter = onOpenConverter,
                onOpenExport = onOpenExport,
                onOpenBudgetCalc = onOpenBudgetCalc,
                onOpenScanQr = onOpenScanQr
            )
        }

        // Monthly Spending Trends Chart
        item {
            MonthlyTrendsChartCard(displayCurrency = displayCurrency)
        }

        // Expense Category Distribution Donut Chart
        item {
            ExpenseCategoryDistributionCard()
        }

        // Live Real-Time Activity Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-Time Activity Feed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${transactions.size} records",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Recent Transactions List
        items(transactions.take(5)) { txn ->
            RecentTransactionItem(
                transaction = txn,
                displayCurrency = displayCurrency,
                onClick = { onSelectTransaction(txn) }
            )
        }
    }
}

@Composable
private fun HeroBalanceCard(
    displayCurrency: AppCurrency,
    totalVolumeInr: Double,
    totalTaxInr: Double,
    onOpenNewPayment: () -> Unit,
    onOpenConverter: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            EmeraldPrimary.copy(alpha = 0.08f),
                            SapphireSecondary.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Total Processed Volume",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+18.4% vs last mo",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    // Currency Tag
                    Surface(
                        onClick = onOpenConverter,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable { onOpenConverter() }
                    ) {
                        Text(
                            text = "${displayCurrency.flag} ${displayCurrency.code}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hero Rupee Figure
                Text(
                    text = CurrencyFormatter.format(totalVolumeInr, displayCurrency),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )

                if (displayCurrency != AppCurrency.INR) {
                    Text(
                        text = "Base INR: ${CurrencyFormatter.formatInr(totalVolumeInr)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary Metric Pillars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Regional Taxes Collected", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = CurrencyFormatter.format(totalTaxInr, displayCurrency),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Gateway Health SLA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "99.99% Operational",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onOpenNewPayment: () -> Unit,
    onOpenNewInvoice: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenBudgetCalc: () -> Unit,
    onOpenScanQr: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        QuickActionButton("Scan QR", Icons.Default.QrCodeScanner, CyanAccent, Modifier.weight(1f).testTag("quick_action_scan_qr")) { onOpenScanQr() }
        QuickActionButton("Pay / Bill", Icons.Default.AddCard, EmeraldPrimary, Modifier.weight(1f).testTag("quick_action_pay")) { onOpenNewPayment() }
        QuickActionButton("Invoice", Icons.Default.Receipt, SapphireSecondary, Modifier.weight(1f).testTag("quick_action_invoice")) { onOpenNewInvoice() }
        QuickActionButton("Convert", Icons.Default.CurrencyExchange, CyanAccent, Modifier.weight(1f).testTag("quick_action_convert")) { onOpenConverter() }
        QuickActionButton("Export", Icons.Default.FileDownload, PurpleAccent, Modifier.weight(1f).testTag("quick_action_export")) { onOpenExport() }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MonthlyTrendsChartCard(displayCurrency: AppCurrency) {
    val months = listOf("May", "Jun", "Jul", "Aug", "Sep", "Oct")
    // Spending points normalized in INR Lakhs (e.g. 4.2L, 5.8L, 6.5L, 7.8L, 8.9L, 10.4L)
    val points = listOf(4.2f, 5.8f, 6.1f, 7.4f, 8.8f, 10.48f)
    var selectedMonthIndex by remember { mutableStateOf(5) }

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
                Column {
                    Text(
                        text = "Monthly Spending & Inflow Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time ledger curve with monthly inspection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Selected point badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${months[selectedMonthIndex]}: ₹${points[selectedMonthIndex]}L",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Canvas Bezier Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val stepX = size.width / (points.size - 1)
                                val tappedIndex = ((offset.x + stepX / 2) / stepX).toInt().coerceIn(0, points.size - 1)
                                selectedMonthIndex = tappedIndex
                            }
                        }
                ) {
                    val maxVal = 12f
                    val minVal = 2f
                    val w = size.width
                    val h = size.height
                    val spacing = w / (points.size - 1)

                    val path = Path()
                    val fillPath = Path()

                    val coords = points.mapIndexed { idx, value ->
                        val x = idx * spacing
                        val y = h - ((value - minVal) / (maxVal - minVal)) * (h - 20f) - 10f
                        Offset(x, y)
                    }

                    // Build smooth curve
                    path.moveTo(coords[0].x, coords[0].y)
                    fillPath.moveTo(coords[0].x, h)
                    fillPath.lineTo(coords[0].x, coords[0].y)

                    for (i in 0 until coords.size - 1) {
                        val p0 = coords[i]
                        val p1 = coords[i + 1]
                        val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                        val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                        path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                        fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                    }

                    fillPath.lineTo(coords.last().x, h)
                    fillPath.close()

                    // Draw gradient fill below curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF10B981).copy(alpha = 0.35f),
                                Color(0xFF10B981).copy(alpha = 0.0f)
                            )
                        )
                    )

                    // Draw Stroke Line
                    drawPath(
                        path = path,
                        color = Color(0xFF10B981),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Dots & selected highlight
                    coords.forEachIndexed { i, pt ->
                        val isSelected = i == selectedMonthIndex
                        if (isSelected) {
                            drawCircle(
                                color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                                radius = 10.dp.toPx(),
                                center = pt
                            )
                        }
                        drawCircle(
                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF10B981),
                            radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Month Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEachIndexed { idx, m ->
                    Text(
                        text = m,
                        fontSize = 11.sp,
                        fontWeight = if (idx == selectedMonthIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (idx == selectedMonthIndex) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseCategoryDistributionCard() {
    val categories = listOf(
        Triple("Software & Cloud", 35f, Color(0xFF3B82F6)),
        Triple("Payroll & Team", 28f, Color(0xFF10B981)),
        Triple("Vendor Invoices", 16f, Color(0xFF8B5CF6)),
        Triple("Regional Taxes", 15f, Color(0xFFEC4899)),
        Triple("Gateway Processing", 6f, Color(0xFFF59E0B))
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Expense Distribution by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Automated categorization across international accounts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart Canvas
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        val strokeWidth = 22.dp.toPx()

                        categories.forEach { (_, percent, color) ->
                            val sweepAngle = (percent / 100f) * 360f
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle - 3f, // gap
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "100%",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Allocated",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legends Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { (name, percent, color) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${percent.toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionItem(
    transaction: Transaction,
    displayCurrency: AppCurrency,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gateway / Type Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(transaction.gateway.primaryColorHex).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (transaction.gateway) {
                        PaymentGateway.STRIPE -> "S"
                        PaymentGateway.PAYPAL -> "P"
                        PaymentGateway.RAZORPAY -> "R"
                        PaymentGateway.BANK_WIRE -> "W"
                        PaymentGateway.UPI_INSTANT -> "UPI"
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color(transaction.gateway.primaryColorHex)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${transaction.customerName} • ${transaction.dateString}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (transaction.type == TransactionType.INCOMING) "+" else "-") +
                            CurrencyFormatter.format(transaction.amountInInr, displayCurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (transaction.type == TransactionType.INCOMING) EmeraldPrimary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Tax: ${CurrencyFormatter.formatInr(transaction.taxAmountInInr)}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
