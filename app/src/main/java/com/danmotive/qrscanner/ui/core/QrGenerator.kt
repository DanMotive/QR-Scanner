package com.danmotive.qrscanner.ui.core

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

// Превращает текст в QR-матрицу. null = не получилось (пустой ввод, слишком длинный текст)
fun encodeQr(text: String, size: Int): BitMatrix? {
    if (text.isEmpty()) return null
    return try {
        QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    } catch (_: Exception) {
        null
    }
}

// Рисует матрицу в Bitmap: чёрные модули на белом фоне (обязательный контраст для считывателей)
fun bitMatrixToBitmap(matrix: BitMatrix): Bitmap {
    val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            bitmap.setPixel(
                x,
                y,
                if (matrix[x, y]) Color.BLACK else Color.WHITE
            )
        }
    }
    return bitmap
}
