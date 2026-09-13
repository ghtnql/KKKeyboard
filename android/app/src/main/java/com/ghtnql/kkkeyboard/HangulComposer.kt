package com.ghtnql.kkkeyboard

class HangulComposer {
    data class Edit(
        val commit: String = "",
        val composing: String? = null,
        val consumed: Boolean = true,
    )

    private var initial = -1
    private var medial = -1
    private var finalIndex = -1

    fun input(ch: Char): Edit {
        val initialIndex = INITIALS.indexOf(ch)
        if (initialIndex >= 0) return inputConsonant(ch, initialIndex)

        val medialIndex = VOWELS.indexOf(ch)
        if (medialIndex >= 0) return inputVowel(ch, medialIndex)

        val pending = flush()
        return Edit(commit = pending + ch, composing = null)
    }

    fun backspace(): Edit {
        when {
            finalIndex > 0 -> {
                val currentFinal = FINALS[finalIndex]
                val split = COMPOUND_FINAL_SPLIT[currentFinal]
                finalIndex = if (split != null) FINALS.indexOf(split.first) else -1
            }
            medial >= 0 -> {
                val currentMedial = VOWELS[medial]
                val base = COMPOUND_MEDIAL_BASE[currentMedial]
                medial = if (base != null) VOWELS.indexOf(base) else -1
            }
            initial >= 0 -> initial = -1
            else -> return Edit(consumed = false)
        }
        return Edit(composing = currentText().ifEmpty { null })
    }

    fun flush(): String {
        val text = currentText()
        clear()
        return text
    }

    fun reset() = clear()

    fun currentText(): String {
        if (initial < 0) {
            return if (medial >= 0) VOWELS[medial].toString() else ""
        }
        if (medial < 0) return INITIALS[initial].toString()

        val jong = if (finalIndex > 0) finalIndex else 0
        val codePoint = HANGUL_BASE + ((initial * 21 + medial) * 28) + jong
        return codePoint.toChar().toString()
    }

    private fun inputConsonant(ch: Char, newInitial: Int): Edit {
        if (initial < 0) {
            if (medial >= 0) {
                val commit = currentText()
                medial = -1
                initial = newInitial
                return Edit(commit = commit, composing = currentText())
            }
            initial = newInitial
            return Edit(composing = currentText())
        }

        if (medial < 0) {
            val commit = currentText()
            initial = newInitial
            return Edit(commit = commit, composing = currentText())
        }

        if (finalIndex < 0) {
            val candidateFinal = FINALS.indexOf(ch)
            if (candidateFinal > 0) {
                finalIndex = candidateFinal
                return Edit(composing = currentText())
            }
        } else {
            val currentFinal = FINALS[finalIndex]
            val compound = compoundFinal(currentFinal, ch)
            if (compound != null) {
                finalIndex = FINALS.indexOf(compound)
                return Edit(composing = currentText())
            }
        }

        val commit = currentText()
        initial = newInitial
        medial = -1
        finalIndex = -1
        return Edit(commit = commit, composing = currentText())
    }

    private fun inputVowel(ch: Char, newMedial: Int): Edit {
        if (initial < 0) {
            if (medial < 0) {
                medial = newMedial
                return Edit(composing = currentText())
            }

            val compound = compoundMedial(VOWELS[medial], ch)
            if (compound != null) {
                medial = VOWELS.indexOf(compound)
                return Edit(composing = currentText())
            }

            val commit = currentText()
            medial = newMedial
            return Edit(commit = commit, composing = currentText())
        }

        if (medial < 0) {
            medial = newMedial
            return Edit(composing = currentText())
        }

        if (finalIndex > 0) {
            val finalChar = FINALS[finalIndex]
            val split = COMPOUND_FINAL_SPLIT[finalChar]

            if (split != null) {
                finalIndex = FINALS.indexOf(split.first)
                val commit = currentText()
                initial = INITIALS.indexOf(split.second)
                medial = newMedial
                finalIndex = -1
                return Edit(commit = commit, composing = currentText())
            }

            finalIndex = -1
            val commit = currentText()
            initial = INITIALS.indexOf(finalChar)
            medial = newMedial
            finalIndex = -1
            return Edit(commit = commit, composing = currentText())
        }

        val compound = compoundMedial(VOWELS[medial], ch)
        if (compound != null) {
            medial = VOWELS.indexOf(compound)
            return Edit(composing = currentText())
        }

        val commit = currentText()
        initial = -1
        medial = newMedial
        finalIndex = -1
        return Edit(commit = commit, composing = currentText())
    }

    private fun clear() {
        initial = -1
        medial = -1
        finalIndex = -1
    }

    companion object {
        private const val HANGUL_BASE = 0xAC00

        private val INITIALS = charArrayOf(
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
        )

        private val VOWELS = charArrayOf(
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
            'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
        )

        private val FINALS = charArrayOf(
            '\u0000', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
            'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
        )

        /**
         * Hot-path lookup without allocating Pair keys for every vowel input.
         */
        private fun compoundMedial(first: Char, second: Char): Char? = when (first) {
            'ㅗ' -> when (second) {
                'ㅏ' -> 'ㅘ'
                'ㅐ' -> 'ㅙ'
                'ㅣ' -> 'ㅚ'
                else -> null
            }
            'ㅜ' -> when (second) {
                'ㅓ' -> 'ㅝ'
                'ㅔ' -> 'ㅞ'
                'ㅣ' -> 'ㅟ'
                else -> null
            }
            'ㅡ' -> if (second == 'ㅣ') 'ㅢ' else null
            else -> null
        }

        private val COMPOUND_MEDIAL_BASE = mapOf(
            'ㅘ' to 'ㅗ', 'ㅙ' to 'ㅗ', 'ㅚ' to 'ㅗ',
            'ㅝ' to 'ㅜ', 'ㅞ' to 'ㅜ', 'ㅟ' to 'ㅜ',
            'ㅢ' to 'ㅡ',
        )

        /**
         * Hot-path lookup without allocating Pair keys for every consonant input.
         */
        private fun compoundFinal(first: Char, second: Char): Char? = when (first) {
            'ㄱ' -> if (second == 'ㅅ') 'ㄳ' else null
            'ㄴ' -> when (second) {
                'ㅈ' -> 'ㄵ'
                'ㅎ' -> 'ㄶ'
                else -> null
            }
            'ㄹ' -> when (second) {
                'ㄱ' -> 'ㄺ'
                'ㅁ' -> 'ㄻ'
                'ㅂ' -> 'ㄼ'
                'ㅅ' -> 'ㄽ'
                'ㅌ' -> 'ㄾ'
                'ㅍ' -> 'ㄿ'
                'ㅎ' -> 'ㅀ'
                else -> null
            }
            'ㅂ' -> if (second == 'ㅅ') 'ㅄ' else null
            else -> null
        }

        private val COMPOUND_FINAL_SPLIT = mapOf(
            'ㄳ' to ('ㄱ' to 'ㅅ'),
            'ㄵ' to ('ㄴ' to 'ㅈ'),
            'ㄶ' to ('ㄴ' to 'ㅎ'),
            'ㄺ' to ('ㄹ' to 'ㄱ'),
            'ㄻ' to ('ㄹ' to 'ㅁ'),
            'ㄼ' to ('ㄹ' to 'ㅂ'),
            'ㄽ' to ('ㄹ' to 'ㅅ'),
            'ㄾ' to ('ㄹ' to 'ㅌ'),
            'ㄿ' to ('ㄹ' to 'ㅍ'),
            'ㅀ' to ('ㄹ' to 'ㅎ'),
            'ㅄ' to ('ㅂ' to 'ㅅ'),
        )
    }
}
