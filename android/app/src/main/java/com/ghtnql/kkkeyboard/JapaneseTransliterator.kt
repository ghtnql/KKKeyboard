package com.ghtnql.kkkeyboard

/**
 * Minimal deterministic Hangul-pronunciation -> Japanese candidate provider.
 *
 * This deliberately starts as a tiny local seed dictionary instead of adding
 * network or heavy parsing work to the keyboard input hot path. The public API
 * is intentionally stable so the seed map can later be replaced by the shared
 * rule/dictionary pipeline without changing candidate UI code.
 */
object JapaneseTransliterator {
    private val seedCandidates: Map<String, List<String>> = mapOf(
        "아리가토" to listOf("ありがとう"),
        "곤니치와" to listOf("こんにちは"),
        "스시" to listOf("すし", "寿司"),
        "코히" to listOf("コーヒー"),
        "킷테" to listOf("きって"),
        "온나" to listOf("おんな"),
        "료코" to listOf("りょこう"),
    )

    val maxInputLength: Int = seedCandidates.keys.maxOfOrNull(String::length) ?: 0

    /**
     * Convenience lookup for non-hot-path callers that may provide surrounding
     * whitespace. Keyboard input should use [candidatesExact] because its token
     * buffer is already normalized and trimming on every key press allocates.
     */
    fun candidates(inputHangul: String): List<String> = candidatesExact(inputHangul.trim())

    /** Allocation-light lookup for the already-normalized IME token. */
    fun candidatesExact(inputHangul: String): List<String> {
        if (inputHangul.isEmpty() || inputHangul.length > maxInputLength) return emptyList()
        return seedCandidates[inputHangul] ?: emptyList()
    }
}
