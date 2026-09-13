package com.ghtnql.kkkeyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardLayoutSettingsTest {
    @Test
    fun persistedHeightValuesRoundTrip() {
        KeyboardHeight.entries.forEach { height ->
            assertEquals(height, KeyboardHeight.fromPersistedValue(height.persistedValue))
        }
    }

    @Test
    fun unknownHeightFallsBackToNormal() {
        assertEquals(KeyboardHeight.NORMAL, KeyboardHeight.fromPersistedValue(null))
        assertEquals(KeyboardHeight.NORMAL, KeyboardHeight.fromPersistedValue("unexpected"))
    }

    @Test
    fun supportedHeightsRemainOrderedAndDistinct() {
        assertEquals(listOf(44, 50, 58), KeyboardHeight.entries.map { it.keyHeightDp })
    }
}
