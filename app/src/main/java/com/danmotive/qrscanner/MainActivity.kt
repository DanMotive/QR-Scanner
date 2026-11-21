package com.danmotive.qrscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.lifecycle.ProcessCameraProvider
import com.danmotive.qrscanner.ui.QrScannerApp
import com.danmotive.qrscanner.ui.theme.QRScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessCameraProvider.getInstance(this)
        enableEdgeToEdge()
        setContent {
            QRScannerTheme {
                QrScannerApp()
            }
        }
    }
}
