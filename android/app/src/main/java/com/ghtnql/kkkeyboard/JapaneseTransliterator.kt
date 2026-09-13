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

    fun candidates(inputHangul: String): List<String> {
        val normalized = inputHangul.trim()
        if (normalized.isEmpty()) return emptyList()
        return seedCandidates[normalized] ?: emptyList()
    }
}
