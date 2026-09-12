package com.ghtnql.kkkeyboard

import android.text.InputType

enum class InputFieldMode {
    TEXT,
    EMAIL,
    URI,
    NUMBER,
    PHONE,
}

object InputFieldModeResolver {
    fun fromInputType(inputType: Int): InputFieldMode {
        return when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER -> InputFieldMode.NUMBER
            InputType.TYPE_CLASS_PHONE -> InputFieldMode.PHONE
            InputType.TYPE_CLASS_TEXT -> when (inputType and InputType.TYPE_MASK_VARIATION) {
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS -> InputFieldMode.EMAIL

                InputType.TYPE_TEXT_VARIATION_URI -> InputFieldMode.URI
                else -> InputFieldMode.TEXT
            }
            else -> InputFieldMode.TEXT
        }
    }
}
