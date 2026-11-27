package com.danmotive.qrscanner.ui.updates

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateCooldownTest {
    @Test
    fun cooldownEndsExactlyOneHourAfterLastCheck() {
        val checkedAt = 1_000L

        assertEquals(
            checkedAt + UPDATE_CHECK_COOLDOWN_MS,
            nextUpdateCheckAt(checkedAt, checkedAt)
        )
        assertNull(
            nextUpdateCheckAt(
                checkedAt,
                checkedAt + UPDATE_CHECK_COOLDOWN_MS
            )
        )
    }
}
