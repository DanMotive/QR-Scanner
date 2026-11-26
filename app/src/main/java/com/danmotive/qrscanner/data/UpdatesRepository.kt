package com.danmotive.qrscanner.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class UpdatesRepository {
    suspend fun latestVersion(): String? = withContext(Dispatchers.IO) {
        val connection = URL(
            "https://api.github.com/repos/DanMotive/QR-Scanner/releases/latest"
        ).openConnection() as HttpURLConnection

        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("User-Agent", "QRScanner")
        connection.setRequestProperty("Accept", "application/vnd.github+json")

        try {
            when (val code = connection.responseCode) {
                200 -> connection.inputStream.bufferedReader().use {
                    JSONObject(it.readText()).getString("tag_name")
                }

                404 -> null
                else -> throw IOException("GitHub HTTP $code")
            }
        } finally {
            connection.disconnect()
        }
    }
}