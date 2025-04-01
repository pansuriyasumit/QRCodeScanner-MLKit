package com.example.qrcodescanner.scanner

interface ScanningResultListener {
    fun onScanned(result: String)
}