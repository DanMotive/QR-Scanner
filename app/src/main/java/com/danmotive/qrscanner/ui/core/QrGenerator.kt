package com.danmotive.qrscanner.ui.core

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

// Текст в матрицу
fun encodeQr(text: String, size: Int): BitMatrix? {
    if (text.isEmpty()) return null
    return try {
        QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    } catch (_: Exception) {
        null
    }
}
// Матрицу в карту
fun bitMatrixToBitmap(matrix: BitMatrix): Bitmap {
    val bitmap = createBitmap(matrix.width, matrix.height)
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            bitmap[x, y] = if (matrix[x, y]) Color.BLACK else Color.WHITE
        }
    }
    return bitmap
}
// Сохранить
fun saveQrToGallery(context: Context, bitmap: Bitmap): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "qr_${System.currentTimeMillis()}.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
    }

    val uri = context.contentResolver.insert(
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        values
    ) ?: return null                      // система не дала запись, надо обработать

    context.contentResolver.openOutputStream(uri)?.use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    } ?: return null

    return uri
}
// Поделиться
fun saveQrToCache(context: Context, bitmap: Bitmap): Uri? {
    val dir = File(context.cacheDir, "qr")
    dir.mkdirs()

    val file = File(dir, "qr_${System.currentTimeMillis()}.png")

    val stream = FileOutputStream(file)
    stream.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}