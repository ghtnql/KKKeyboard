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
        if (medialIndex >= 0) return inputVowel(medialIndex)

        val pending = flush()
        return Edit(commit = pending + ch, composing = null)
    }

    fun backspace(): Edit {
        when {
            finalIndex > 0 -> finalIndex = -1
            medial >= 0 -> medial = -1
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
        if (initial < 0) return ""
        if (medial < 0) return INITIALS[initial].toString()

        val jong = if (finalIndex > 0) finalIndex else 0
        val codePoint = HANGUL_BASE + ((initial * 21 + medial) * 28) + jong
        return codePoint.toChar().toString()
    }

    private fun inputConsonant(ch: Char, newInitial: Int): Edit {
        if (initial < 0) {
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
        }

        val commit = currentText()
        initial = newInitial
        medial = -1
        finalIndex = -1
        return Edit(commit = commit, composing = currentText())
    }

    private fun inputVowel(newMedial: Int): Edit {
        if (initial < 0) {
            initial = SILENT_IEUNG_INDEX
            medial = newMedial
            return Edit(composing = currentText())
        }

        if (medial < 0) {
            medial = newMedial
            return Edit(composing = currentText())
        }

        if (finalIndex > 0) {
            val lastConsonant = FINALS[finalIndex]
            finalIndex = -1
            val commit = currentText()

            initial = INITIALS.indexOf(lastConsonant).takeIf { it >= 0 } ?: SILENT_IEUNG_INDEX
            medial = newMedial
            finalIndex = -1
            return Edit(commit = commit, composing = currentText())
        }

        val commit = currentText()
        initial = SILENT_IEUNG_INDEX
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
        private const val SILENT_IEUNG_INDEX = 11

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
    }
}
