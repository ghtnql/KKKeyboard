package com.ghtnql.kkkeyboard

import android.content.Context

/**
 * Small, allocation-free-at-input-time layout settings surface.
 * Preferences are read when the IME view is created/restarted, never per key press.
 */
enum class KeyboardHeight(val persistedValue: String, val keyHeightDp: Int) {
    COMPACT("compact", 44),
    NORMAL("normal", 50),
    TALL("tall", 58),
    ;

    companion object {
        fun fromPersistedValue(value: String?): KeyboardHeight =
            entries.firstOrNull { it.persistedValue == value } ?: NORMAL
    }
}

enum class KeyboardOrientation(val preferenceSuffix: String) {
    PORTRAIT("portrait"),
    LANDSCAPE("landscape"),
    ;

    companion object {
        // Android Configuration.ORIENTATION_LANDSCAPE is 2. Keep the mapper pure so
        // local JVM tests do not need Android framework classes on their execution path.
        private const val ANDROID_ORIENTATION_LANDSCAPE = 2

        fun fromConfigurationOrientation(orientation: Int): KeyboardOrientation =
            if (orientation == ANDROID_ORIENTATION_LANDSCAPE) LANDSCAPE else PORTRAIT
    }
}

object KeyboardLayoutSettings {
    private const val PREFS_NAME = "keyboard_layout"
    private const val LEGACY_KEY_HEIGHT = "height"
    private const val LEGACY_KEY_NUMBER_ROW = "number_row"

    private fun heightKey(orientation: KeyboardOrientation) = "height_${orientation.preferenceSuffix}"
    private fun numberRowKey(orientation: KeyboardOrientation) = "number_row_${orientation.preferenceSuffix}"

    fun readHeight(context: Context, orientation: KeyboardOrientation): KeyboardHeight {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val value = preferences.getString(
            heightKey(orientation),
            preferences.getString(LEGACY_KEY_HEIGHT, null),
        )
        return KeyboardHeight.fromPersistedValue(value)
    }

    fun writeHeight(context: Context, orientation: KeyboardOrientation, height: KeyboardHeight) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(heightKey(orientation), height.persistedValue)
            .apply()
    }

    fun readNumberRowEnabled(context: Context, orientation: KeyboardOrientation): Boolean {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (preferences.contains(numberRowKey(orientation))) {
            preferences.getBoolean(numberRowKey(orientation), false)
        } else {
            preferences.getBoolean(LEGACY_KEY_NUMBER_ROW, false)
        }
    }

    fun writeNumberRowEnabled(context: Context, orientation: KeyboardOrientation, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(numberRowKey(orientation), enabled)
            .apply()
    }
}
