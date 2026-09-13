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

object KeyboardLayoutSettings {
    private const val PREFS_NAME = "keyboard_layout"
    private const val KEY_HEIGHT = "height"

    fun readHeight(context: Context): KeyboardHeight {
        val value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HEIGHT, null)
        return KeyboardHeight.fromPersistedValue(value)
    }

    fun writeHeight(context: Context, height: KeyboardHeight) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HEIGHT, height.persistedValue)
            .apply()
    }
}
