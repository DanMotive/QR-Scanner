package com.danmotive.qrscanner.ui.core

import com.google.zxing.BarcodeFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BarcodeGeneratorTest {
    @Test
    fun allListedFormatsEncodeValidInput() {
        val examples = mapOf(
            BarcodeFormat.QR_CODE to "https://example.com",
            BarcodeFormat.DATA_MATRIX to "Data Matrix",
            BarcodeFormat.AZTEC to "Aztec",
            BarcodeFormat.PDF_417 to "PDF417",
            BarcodeFormat.EAN_13 to "5901234123457",
            BarcodeFormat.EAN_8 to "96385074",
            BarcodeFormat.UPC_A to "036000291452",
            BarcodeFormat.UPC_E to "04252614",
            BarcodeFormat.CODE_128 to "Code 128",
            BarcodeFormat.CODE_39 to "CODE39",
            BarcodeFormat.CODE_93 to "CODE93",
            BarcodeFormat.ITF to "123456",
            BarcodeFormat.CODABAR to "A12345B"
        )

        assertEquals(examples.keys, generatableBarcodeFormats.toSet())
        examples.forEach { (format, input) ->
            assertNotNull(format.name, encodeBarcode(input, format, 1024, 512))
        }
    }
}
