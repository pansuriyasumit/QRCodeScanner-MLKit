package com.example.qrcodescanner.scanner

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class MLKitBarcodeAnalyzer(private val listener: ScanningResultListener) : ImageAnalysis.Analyzer {

    private var isScanning: Boolean = false

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && !isScanning) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            // Pass image to an ML Kit Vision API

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_ALL_FORMATS)
                .enableAllPotentialBarcodes()
                .build()

            val scanner = BarcodeScanning.getClient(options)

            isScanning = true
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // Task completed successfully
                    barcodes.firstOrNull().let { barcode ->
                        val rawValue = barcode?.rawValue
                        rawValue?.let {
                            listener.onScanned(it)
                        }
                    }

                    isScanning = false
                    imageProxy.close()
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    isScanning = false
                    imageProxy.close()
                }
        }
    }
}