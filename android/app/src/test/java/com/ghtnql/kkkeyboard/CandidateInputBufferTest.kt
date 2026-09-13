package com.ghtnql.kkkeyboard

import org.junit.Assert.assertEquals
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
    fun `clear drops session token`() {
        val buffer = CandidateInputBuffer()
        buffer.apply(HangulComposer.Edit(commit = "곤니치"))
        buffer.clear()

        assertEquals("", buffer.current("와"))
    }
}
