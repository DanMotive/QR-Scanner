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
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileOutputStream

val generatableBarcodeFormats = listOf(
    BarcodeFormat.QR_CODE,
    BarcodeFormat.DATA_MATRIX,
    BarcodeFormat.AZTEC,
    BarcodeFormat.PDF_417,
    BarcodeFormat.EAN_13,
    BarcodeFormat.EAN_8,
    BarcodeFormat.UPC_A,
    BarcodeFormat.UPC_E,
    BarcodeFormat.CODE_128,
    BarcodeFormat.CODE_39,
    BarcodeFormat.CODE_93,
    BarcodeFormat.ITF,
    BarcodeFormat.CODABAR
)

fun encodeBarcode(text: String, format: BarcodeFormat, width: Int, height: Int): BitMatrix? {
    if (text.isEmpty()) return null
    return try {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )

        MultiFormatWriter().encode(
            text,
            format,
            width,
            height,
            hints
        )
    } catch (_: Exception) {
        null
    }
}

fun bitMatrixToBitmap(matrix: BitMatrix): Bitmap {
    val bitmap = createBitmap(matrix.width, matrix.height)
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            bitmap[x, y] = if (matrix[x, y]) Color.BLACK else Color.WHITE
        }
    }
    return bitmap
}

fun saveBarcodeToGallery(context: Context, bitmap: Bitmap): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "barcode_${System.currentTimeMillis()}.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
    }

    val uri = context.contentResolver.insert(
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        values
    ) ?: return null

    context.contentResolver.openOutputStream(uri)?.use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    } ?: return null

    return uri
}

fun saveBarcodeToCache(context: Context, bitmap: Bitmap): Uri? {
    val dir = File(context.cacheDir, "qr")
    dir.mkdirs()

    val file = File(dir, "barcode_${System.currentTimeMillis()}.png")

    val stream = FileOutputStream(file)
    stream.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}
