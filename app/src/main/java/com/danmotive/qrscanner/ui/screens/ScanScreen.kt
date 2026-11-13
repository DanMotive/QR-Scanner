package com.danmotive.qrscanner.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var result by remember { mutableStateOf<String?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    val scanned = remember { AtomicBoolean(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Сканирование QR-кода")

        Spacer(modifier = Modifier.height(16.dp))

        if (hasPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also {
                        it.scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewView = it
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            )

            @OptIn(ExperimentalGetImage::class)
            DisposableEffect(previewView, lifecycleOwner) {
                val view = previewView
                val executor = Executors.newSingleThreadExecutor()

                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                )

                var cameraProvider: ProcessCameraProvider? = null

                if (view != null) {
                    val future = ProcessCameraProvider.getInstance(context)

                    future.addListener({
                        val provider = future.get()
                        cameraProvider = provider

                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.surfaceProvider = view.surfaceProvider
                            }

                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(
                                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                            )
                            .build()

                        analysis.setAnalyzer(
                            executor,
                            @OptIn(ExperimentalGetImage::class)
                            object : ImageAnalysis.Analyzer {
                                override fun analyze(imageProxy: ImageProxy) {
                                    val mediaImage = imageProxy.image

                                    if (mediaImage == null || scanned.get()) {
                                        imageProxy.close()
                                        return
                                    }

                                    val inputImage = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )

                                    scanner.process(inputImage)
                                        .addOnSuccessListener(
                                            ContextCompat.getMainExecutor(context)
                                        ) { barcodes ->
                                            val value = barcodes.firstOrNull()?.rawValue

                                            if (value != null &&
                                                scanned.compareAndSet(false, true)
                                            ) {
                                                result = value
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                }
                            }
                        )

                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                            )
                        } catch (e: Exception) {
                            result = "Ошибка камеры: ${e.message}"
                        }

                    }, ContextCompat.getMainExecutor(context))
                }

                onDispose {
                    cameraProvider?.unbindAll()
                    scanner.close()
                    executor.shutdown()
                }
            }
        } else {
            Text("Для сканирования нужен доступ к камере")

            Button(
                onClick = {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            ) {
                Text("Разрешить камеру")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (result != null) {
            Text("Результат: $result")

            Button(
                onClick = {
                    result = null
                    scanned.set(false)
                }
            ) {
                Text("Сканировать снова")
            }
        } else if (hasPermission) {
            Text("Наведите камеру на QR-код")
        }
    }
}