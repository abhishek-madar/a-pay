package com.example.util

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(
                BarcodeFormat.QR_CODE,
                BarcodeFormat.DATA_MATRIX,
                BarcodeFormat.UPC_A,
                BarcodeFormat.EAN_13
            ),
            DecodeHintType.TRY_HARDER to true
        )
        setHints(hints)
    }

    private var hasScanned = false

    override fun analyze(image: ImageProxy) {
        if (hasScanned) {
            image.close()
            return
        }

        try {
            val plane = image.planes[0]
            val buffer: ByteBuffer = plane.buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val width = image.width
            val height = image.height
            val rowStride = plane.rowStride

            val source = PlanarYUVLuminanceSource(
                data,
                rowStride,
                height,
                0,
                0,
                width,
                height,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(binaryBitmap)

            result?.text?.let { scannedText ->
                if (scannedText.isNotBlank()) {
                    hasScanned = true
                    onQrCodeScanned(scannedText)
                }
            }
        } catch (_: NotFoundException) {
            // Frame does not contain QR code, normal
        } catch (_: Exception) {
            // Ignore other frame read exceptions
        } finally {
            image.close()
        }
    }

    fun reset() {
        hasScanned = false
    }
}
