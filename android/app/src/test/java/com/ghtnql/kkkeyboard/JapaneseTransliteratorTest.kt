package com.ghtnql.kkkeyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JapaneseTransliteratorTest {
    @Test
    fun returnsSeedCandidateForArigato() {
        assertEquals(listOf("ありがとう"), JapaneseTransliterator.candidates("아리가토"))
    }

    @Test
    fun preservesOrthographicGreetingException() {
        assertEquals(listOf("こんにちは"), JapaneseTransliterator.candidates("곤니치와"))
    }

    @Test
    fun returnsMultipleCandidatesInStableOrder() {
        assertEquals(listOf("すし", "寿司"), JapaneseTransliterator.candidates("스시"))
    }

    @Test
    fun ignoresOuterWhitespaceWithoutNetworkOrFallbackGuessing() {
        assertEquals(listOf("ありがとう"), JapaneseTransliterator.candidates("  아리가토  "))
    }

    @Test
    fun unsupportedInputReturnsNoCandidate() {
        assertTrue(JapaneseTransliterator.candidates("미등록입력").isEmpty())
    }
}
