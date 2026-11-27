package com.danmotive.qrscanner.ui.updates

internal const val UPDATE_CHECK_COOLDOWN_MS = 60 * 60 * 1000L

internal fun nextUpdateCheckAt(lastCheckedAt: Long?, now: Long): Long? =
    lastCheckedAt
        ?.plus(UPDATE_CHECK_COOLDOWN_MS)
        ?.takeIf { it > now }
