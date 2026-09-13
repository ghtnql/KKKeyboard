package com.ghtnql.kkkeyboard

import android.content.res.Configuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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

    @Test
    fun orientationProfilesMapFromAndroidConfiguration() {
        assertEquals(
            KeyboardOrientation.PORTRAIT,
            KeyboardOrientation.fromConfigurationOrientation(Configuration.ORIENTATION_PORTRAIT),
        )
        assertEquals(
            KeyboardOrientation.LANDSCAPE,
            KeyboardOrientation.fromConfigurationOrientation(Configuration.ORIENTATION_LANDSCAPE),
        )
    }

    @Test
    fun orientationProfilesUseDistinctPreferenceNamespaces() {
        assertNotEquals(
            KeyboardOrientation.PORTRAIT.preferenceSuffix,
            KeyboardOrientation.LANDSCAPE.preferenceSuffix,
        )
    }
}
