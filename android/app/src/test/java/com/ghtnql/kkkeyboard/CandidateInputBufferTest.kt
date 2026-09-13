package com.ghtnql.kkkeyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CandidateInputBufferTest {
    @Test
    fun `builds current token from committed and composing Hangul`() {
        val buffer = CandidateInputBuffer()
        buffer.apply(HangulComposer.Edit(commit = "아리", composing = "가"))

        assertEquals("아리가", buffer.current("가"))
    }

    @Test
    fun `non Hangul commit clears token boundary`() {
        val buffer = CandidateInputBuffer()
        buffer.apply(HangulComposer.Edit(commit = "아리"))
        buffer.apply(HangulComposer.Edit(commit = " "))

        assertEquals("", buffer.current(""))
    }

    @Test
    fun `removes last committed syllable after fallback backspace`() {
        val buffer = CandidateInputBuffer()
        buffer.apply(HangulComposer.Edit(commit = "아리가"))
        buffer.removeCommittedCodePoint()

        assertEquals("아리", buffer.current(""))
    }

    @Test
    fun `clear drops committed session token while preserving current composition`() {
        val buffer = CandidateInputBuffer()
        buffer.apply(HangulComposer.Edit(commit = "곤니치"))
        buffer.clear()

        assertEquals("와", buffer.current("와"))
    }

    @Test
    fun `unsupported japanese lookup leaves tracked Hangul available for continued input`() {
        val buffer = CandidateInputBuffer()
        buffer.apply(HangulComposer.Edit(commit = "미등록", composing = "입"))

        val sourceBeforeLookup = buffer.current("입")
        assertTrue(JapaneseTransliterator.candidates(sourceBeforeLookup).isEmpty())
        assertEquals("미등록입", buffer.current("입"))

        buffer.apply(HangulComposer.Edit(commit = "입", composing = "력"))
        assertEquals("미등록입력", buffer.current("력"))
    }

    @Test
    fun `lookup skips string construction after dictionary maximum while preserving source`() {
        val buffer = CandidateInputBuffer()
        val longToken = "가".repeat(JapaneseTransliterator.maxInputLength + 32)
        buffer.apply(HangulComposer.Edit(commit = longToken))

        assertNull(buffer.currentForLookup("나", JapaneseTransliterator.maxInputLength))
        assertEquals(longToken + "나", buffer.current("나"))
    }
}
