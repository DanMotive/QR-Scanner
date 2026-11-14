package com.danmotive.qrscanner.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.danmotive.qrscanner.R
import com.danmotive.qrscanner.ui.core.isWebUrl
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import androidx.core.net.toUri


@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val openErrorMessage = stringResource(R.string.error_link)
    val copiedMessage = stringResource(R.string.copied)

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var result by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
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
        Text(stringResource(R.string.scan_title))

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
                    .weight(1f)
            )

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
                            object : ImageAnalysis.Analyzer {
                                @androidx.annotation.OptIn(ExperimentalGetImage::class)
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
                            error = e.message
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
            Text(stringResource(R.string.camera_permission_needed))

            Button(
                onClick = {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            ) {
                Text(stringResource(R.string.allow_camera))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        error?.let {
            Text(stringResource(R.string.camera_error, it))
        }

        result?.let { value ->
            Text(stringResource(R.string.result_format, value))

            Spacer(modifier = Modifier.height(8.dp))

            val isLink = isWebUrl(value) // проверяет есть ли :// в расшифровке

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isLink) {
                    Button(onClick = {
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, value.toUri())
                            )
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                openErrorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) {
                        Text(stringResource(R.string.open_link))
                    }
                }
                Button(onClick = {
                    clipboard.setText(AnnotatedString(value))
                    Toast.makeText(
                        context,
                        copiedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    Text(stringResource(R.string.copy))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    result = null
                    scanned.set(false)
                }
            ) {
                Text(stringResource(R.string.scan_again))
            }
        }

        if (result == null && hasPermission) {
            Text(stringResource(R.string.point_camera_hint))
        }
    }
}