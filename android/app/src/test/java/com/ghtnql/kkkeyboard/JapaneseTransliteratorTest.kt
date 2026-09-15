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
    fun coversLongVowelSokuonNasalAndYouonFixtures() {
        assertEquals(listOf("コーヒー"), JapaneseTransliterator.candidates("코히"))
        assertEquals(listOf("きって"), JapaneseTransliterator.candidates("킷테"))
        assertEquals(listOf("おんな"), JapaneseTransliterator.candidates("온나"))
        assertEquals(listOf("りょこう"), JapaneseTransliterator.candidates("료코"))
    }

    @Test
    fun ignoresOuterWhitespaceWithoutNetworkOrFallbackGuessing() {
        assertEquals(listOf("ありがとう"), JapaneseTransliterator.candidates("  아리가토  "))
    }

    @Test
    fun exactLookupDoesNotNormalizeHotPathInput() {
        assertEquals(listOf("ありがとう"), JapaneseTransliterator.candidatesExact("아리가토"))
        assertTrue(JapaneseTransliterator.candidatesExact(" 아리가토 ").isEmpty())
    }

    @Test
    fun unsupportedInputReturnsNoCandidate() {
        assertTrue(JapaneseTransliterator.candidates("미등록입력").isEmpty())
        assertTrue(JapaneseTransliterator.candidatesExact("").isEmpty())
    }
}
