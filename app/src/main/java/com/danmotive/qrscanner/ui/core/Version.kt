package com.danmotive.qrscanner.ui.core

fun isNewerVersion(current: String, remote: String): Boolean {
    fun parts(version: String): List<Int>? {
        val value = version.removePrefix("v")
        if (!value.matches(Regex("\\d+(\\.\\d+)*"))) return null

        return value.split(".").map {
            it.toIntOrNull() ?: return null
        }
    }

    val local = parts(current) ?: return false
    val latest = parts(remote) ?: return false

    for (index in 0 until maxOf(local.size, latest.size)) {
        val a = local.getOrElse(index) { 0 }
        val b = latest.getOrElse(index) { 0 }
        if (a != b) return b > a
    }

    return false
}
