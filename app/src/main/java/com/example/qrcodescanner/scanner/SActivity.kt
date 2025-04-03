package com.example.qrcodescanner.scanner

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class SActivity : AppCompatActivity() {

    private lateinit var viewModel: YourViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(YourViewModel::class.java)
        viewModel.cameraRequestParam.value = CameraRequestParam("macAddress", "otherParams")
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.startPolling()
            }
        }

        viewModel.installNewCameraStatus.observe(this) { result ->
            when (result) {
                is HomeSecurityInstallCameraResult.HomeSecurityInstallCameraSuccess -> {
                    // Handle success
                }
                is HomeSecurityInstallCameraResult.HomeSecurityInstallCameraFailure -> {
                    // Handle failure
                }
            }
        }
    }
}