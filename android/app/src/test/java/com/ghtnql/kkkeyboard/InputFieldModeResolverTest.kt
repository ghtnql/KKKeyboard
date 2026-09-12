package com.ghtnql.kkkeyboard

import android.text.InputType
import org.junit.Assert.assertEquals
import org.junit.Test

class InputFieldModeResolverTest {
    @Test
    fun resolvesNumericAndPhoneFields() {
        assertEquals(
            InputFieldMode.NUMBER,
            InputFieldModeResolver.fromInputType(InputType.TYPE_CLASS_NUMBER),
        )
        assertEquals(
            InputFieldMode.PHONE,
            InputFieldModeResolver.fromInputType(InputType.TYPE_CLASS_PHONE),
        )
    }

    @Test
    fun resolvesEmailAndUriTextVariations() {
        assertEquals(
            InputFieldMode.EMAIL,
            InputFieldModeResolver.fromInputType(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            ),
        )
        assertEquals(
            InputFieldMode.EMAIL,
            InputFieldModeResolver.fromInputType(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
            ),
        )
        assertEquals(
            InputFieldMode.URI,
            InputFieldModeResolver.fromInputType(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
            ),
        )
    }

    @Test
    fun defaultsToTextForNormalAndUnknownClasses() {
        assertEquals(
            InputFieldMode.TEXT,
            InputFieldModeResolver.fromInputType(InputType.TYPE_CLASS_TEXT),
        )
        assertEquals(InputFieldMode.TEXT, InputFieldModeResolver.fromInputType(0))
    }
}
