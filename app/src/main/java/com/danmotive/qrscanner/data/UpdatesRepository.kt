package com.danmotive.qrscanner.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class UpdatesRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "updates",
        Context.MODE_PRIVATE
    )

    fun lastCheckedAt(): Long? = preferences.getLong(KEY_LAST_CHECKED_AT, 0L)
        .takeIf { it > 0L }

    fun cachedLatestVersion(): String? = preferences.getString(KEY_LATEST_VERSION, null)

    fun markCheckStarted(timestamp: Long) {
        preferences.edit().putLong(KEY_LAST_CHECKED_AT, timestamp).apply()
    }

    fun cacheLatestVersion(version: String?) {
        preferences.edit().apply {
            if (version == null) {
                remove(KEY_LATEST_VERSION)
            } else {
                putString(KEY_LATEST_VERSION, version)
            }
        }.apply()
    }

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

    private companion object {
        const val KEY_LAST_CHECKED_AT = "last_checked_at"
        const val KEY_LATEST_VERSION = "latest_version"
    }
}
