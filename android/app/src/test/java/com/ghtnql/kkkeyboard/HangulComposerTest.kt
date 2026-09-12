package com.ghtnql.kkkeyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HangulComposerTest {
    @Test
    fun composesBasicSyllable() {
        val c = HangulComposer()
        c.input('ㄱ')
        c.input('ㅏ')
        assertEquals("가", c.currentText())
    }

    @Test
    fun composesFinalConsonant() {
        val c = HangulComposer()
        c.input('ㄱ')
        c.input('ㅏ')
        c.input('ㄴ')
        assertEquals("간", c.currentText())
    }

    @Test
    fun movesSimpleFinalToNextSyllableWhenVowelFollows() {
        val c = HangulComposer()
        c.input('ㄱ')
        c.input('ㅏ')
        c.input('ㄴ')
        val edit = c.input('ㅏ')
        assertEquals("가", edit.commit)
        assertEquals("나", edit.composing)
    }

    @Test
    fun backspaceWalksCompositionBack() {
        val c = HangulComposer()
        c.input('ㄱ')
        c.input('ㅏ')
        c.input('ㄴ')
        assertEquals("가", c.backspace().composing)
        assertEquals("ㄱ", c.backspace().composing)
        val last = c.backspace()
        assertEquals(null, last.composing)
        assertFalse(c.backspace().consumed)
    }

    @Test
    fun standaloneVowelUsesSilentIeung() {
        val c = HangulComposer()
        c.input('ㅏ')
        assertEquals("아", c.currentText())
    }
}
