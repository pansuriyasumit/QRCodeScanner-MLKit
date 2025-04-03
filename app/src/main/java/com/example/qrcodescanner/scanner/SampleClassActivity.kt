package com.example.qrcodescanner.scanner

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class SampleClassActivity : ViewModel() {

    private val _installNewCameraStatus = MutableLiveData<HomeSecurityInstallCameraResult>()
    val installNewCameraStatus: LiveData<HomeSecurityInstallCameraResult> = _installNewCameraStatus

    var cameraRequestParam: MutableLiveData<CameraRequestParam> = MutableLiveData()

    fun getInstallCameraStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                HomeSecurityDataStore.getInstallCameraStatus(macAddress = cameraRequestParam.value?.macAddress.toString())
                    .onSuccess { response ->
                        withContext(Dispatchers.Main) {
                            _installNewCameraStatus.postValue(
                                HomeSecurityInstallCameraResult.HomeSecurityInstallCameraSuccess(response)
                            )
                        }
                    }
                    .onFailure { failure ->
                        withContext(Dispatchers.Main) {
                            _installNewCameraStatus.postValue(
                                HomeSecurityInstallCameraResult.HomeSecurityInstallCameraFailure(failure)
                            )
                        }
                    }

            }catch (e: Exception){
                // Handle network exception
            }
        }
    }

    fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                getInstallCameraStatus()
                delay(5000)
            }
        }
    }


}


sealed class HomeSecurityInstallCameraResult {
    data class HomeSecurityInstallCameraSuccess(val response: Any) : HomeSecurityInstallCameraResult()
    data class HomeSecurityInstallCameraFailure(val failure: Throwable) : HomeSecurityInstallCameraResult()
}

data class CameraRequestParam(
    val macAddress: String,
    val otherParameters: String
)

object HomeSecurityDataStore{
    suspend fun getInstallCameraStatus(macAddress: String): Result<Any> {
        // Implement your network request here, for example, using Retrofit
        delay(1000)
        return if(macAddress.isNotBlank()){
            Result.success("Successful Response")
        } else{
            Result.failure(Exception("Network Error"))
        }

    }
}