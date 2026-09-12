package com.ghtnql.kkkeyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TwoBeolsikLayoutTest {
    @Test
    fun shiftedConsonantsUseDoubleJamo() {
        assertEquals("ㅃ", TwoBeolsikLayout.labelFor("ㅂ", shifted = true))
        assertEquals("ㅉ", TwoBeolsikLayout.labelFor("ㅈ", shifted = true))
        assertEquals("ㄸ", TwoBeolsikLayout.labelFor("ㄷ", shifted = true))
        assertEquals("ㄲ", TwoBeolsikLayout.labelFor("ㄱ", shifted = true))
        assertEquals("ㅆ", TwoBeolsikLayout.labelFor("ㅅ", shifted = true))
    }

    @Test
    fun shiftedVowelsUseExpectedVariants() {
        assertEquals("ㅒ", TwoBeolsikLayout.labelFor("ㅐ", shifted = true))
        assertEquals("ㅖ", TwoBeolsikLayout.labelFor("ㅔ", shifted = true))
    }

    @Test
    fun keysWithoutShiftVariantStayUnchanged() {
        assertEquals("ㅏ", TwoBeolsikLayout.labelFor("ㅏ", shifted = true))
        assertEquals("ㅏ", TwoBeolsikLayout.labelFor("ㅏ", shifted = false))
        assertFalse(TwoBeolsikLayout.hasShiftVariant("ㅏ"))
        assertTrue(TwoBeolsikLayout.hasShiftVariant("ㄱ"))
    }
}
