package com.example.qrcodescanner.scanner

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.qrcodescanner.databinding.ActivityScanBinding
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    private var _binding: ActivityScanBinding? = null
    private val binding
        get() = _binding!!

    //QRCode Scanner
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var mCameraProvider: ProcessCameraProvider? = null
    private lateinit var camera: Camera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        reBindCameraProvider()
    }

    private fun reBindCameraProvider() {
        cameraProviderFuture.addListener({
            val isBackCamera =
                cameraProviderFuture.get().hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            when {
                isBackCamera -> {
                    mCameraProvider = cameraProviderFuture.get()
                    bindPreview(mCameraProvider)
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Bind QRCode Scanner Preview
     */
    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        if (isDestroyed || isFinishing) {
            return
        }

        cameraProvider?.unbindAll()

        val preview: Preview = Preview.Builder()
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(binding.previewView.width, binding.previewView.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val orientationEventListener = object : OrientationEventListener(this as Context) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation: Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageAnalysis.targetRotation = rotation
            }
        }
        orientationEventListener.enable()

        //switch the analyzers here, i.e. MLKitBarcodeAnalyzer
        class ScanningListener : ScanningResultListener {
            override fun onScanned(result: String) {
                mCameraProvider?.unbindAll()
                handleQrResult(result)
            }

            private fun handleQrResult(result: String) {
                result.let {
                    Log.e("MLKitBarcodeAnalyzer", "Scanned result: $it")
                    reBindCameraProvider()
                }
            }
        }

        val analyzer: ImageAnalysis.Analyzer = MLKitBarcodeAnalyzer(ScanningListener())
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        camera = cameraProvider?.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)!!
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        _binding = null
    }

}