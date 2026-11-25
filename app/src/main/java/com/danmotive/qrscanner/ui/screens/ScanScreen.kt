package com.danmotive.qrscanner.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.danmotive.qrscanner.R
import com.danmotive.qrscanner.ui.components.ResultCard
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun ScanScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val openErrorMessage = stringResource(R.string.error_link)
    val copiedMessage = stringResource(R.string.copied)
    val qrNotFoundMessage = stringResource(R.string.qr_not_found)
    val scanErrorMessage = stringResource(R.string.scan_error)
    val imageOpenErrorMessage = stringResource(R.string.image_open_error)
    val haptic = LocalHapticFeedback.current

    var hasPermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var permissionDeniedPermanently by rememberSaveable { mutableStateOf(false) }

    var result by rememberSaveable { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    val hasFlash = remember(camera) { camera?.cameraInfo?.hasFlashUnit() == true }
    val scanned = remember { AtomicBoolean(result != null) }
    var torchOn by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        permissionRequested = true
        if (granted) {
            permissionDeniedPermanently = false
        } else {
            val showRationale = context.findActivity()?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.CAMERA
                )
            } ?: true
            permissionDeniedPermanently = !showRationale
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->

        if (uri != null) {
            result = null
            error = null
            scanned.set(true)

            try {
                val image = InputImage.fromFilePath(context, uri)

                val photoScanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build()
                )

                photoScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val barcode = barcodes.firstOrNull()

                        val value = barcode?.rawValue ?: barcode?.displayValue
                        if (value != null) {
                            result = value
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else {
                            Toast.makeText(
                                context,
                                qrNotFoundMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                            scanned.set(false)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            scanErrorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                        scanned.set(false)
                    }
                    .addOnCompleteListener {
                        photoScanner.close()
                    }

            } catch (_: Exception) {
                scanned.set(false)
                Toast.makeText(
                    context,
                    imageOpenErrorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                hasPermission = granted
                if (granted) {
                    permissionDeniedPermanently = false
                } else if (permissionRequested) {
                    val showRationale = context.findActivity()?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            it,
                            Manifest.permission.CAMERA
                        )
                    } ?: true
                    permissionDeniedPermanently = !showRationale
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(result) {
        scanned.set(result != null)
        if (result != null) {
            if (camera?.cameraInfo?.hasFlashUnit() == true) {
                camera?.cameraControl?.enableTorch(false)
            }
            torchOn = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        if (hasPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also {
                        it.scaleType = PreviewView.ScaleType.FILL_CENTER
                        it.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        previewView = it
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
            )

            DisposableEffect(previewView, lifecycleOwner) {
                val view = previewView
                val executor = Executors.newSingleThreadExecutor()

                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()

                val scanner = AtomicReference<BarcodeScanner?>(null)

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

                                    val client = scanner.get()
                                        ?: BarcodeScanning.getClient(options)
                                            .also { scanner.set(it) }

                                    client.process(inputImage)
                                        .addOnSuccessListener(
                                            ContextCompat.getMainExecutor(context)
                                        ) { barcodes ->
                                            val value = barcodes.firstOrNull()?.rawValue
                                            if (value != null && scanned.compareAndSet(
                                                    false,
                                                    true
                                                )
                                            ) {
                                                result = value
                                                haptic.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
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
                            camera = provider.bindToLifecycle(
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
                    camera = null
                    torchOn = false
                    cameraProvider?.unbindAll()
                    scanner.get()?.close()
                    executor.shutdown()
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(
                        if (permissionDeniedPermanently) {
                            R.string.camera_permission_blocked
                        } else {
                            R.string.camera_permission_needed
                        }
                    )
                )
                Button(
                    onClick = {
                        if (permissionDeniedPermanently) {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        } else {
                            launcher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    // Кнопка, если нет разрешения к камере
                    Text(
                        stringResource(
                            if (permissionDeniedPermanently) {
                                R.string.open_settings
                            } else {
                                R.string.allow_camera
                            }
                        )
                    )
                }
            }
        }

        error?.let {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.camera_error, it))
            }
        }

        result?.let { value ->
            ResultCard(
                value = value,
                // Кнопка открытия ссылки
                onOpenLink = {
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
                },
                // Кнопка копирования
                onCopy = {
                    clipboard.setText(AnnotatedString(value))
                    Toast.makeText(
                        context,
                        copiedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                },
                // Кнопка скана снова
                onScanAgain = {
                    result = null
                    scanned.set(false)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
// Подсказка в центре
        if (result == null && hasPermission) {
            Text(
                stringResource(R.string.point_camera_hint),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Кнопка импорта фотографии
        if (result == null) {
            FilledTonalButton(
                onClick = {
                    photoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Text(stringResource(R.string.scan_from_photo))
            }
        }

        // Кнопка вспышки
        if (hasFlash) {
            IconButton(
                onClick = {
                    val c = camera
                    if (c != null) {
                        torchOn = !torchOn
                        c.cameraControl.enableTorch(torchOn)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_flash),
                    contentDescription = stringResource(R.string.flash),
                    tint = if (torchOn) Color(0xFFFFC107) else Color.White
                )
            }
        }
    }
}
