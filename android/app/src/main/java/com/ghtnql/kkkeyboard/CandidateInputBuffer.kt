package com.ghtnql.kkkeyboard

/**
 * Tracks only the current contiguous Hangul token for Japanese candidate lookup.
 *
 * This avoids querying the editor or running parsing work on every key press. The
 * buffer is intentionally tiny and local to the IME session; it never records or
 * persists full user messages.
 */
class CandidateInputBuffer {
    private val token = StringBuilder()

    fun apply(edit: HangulComposer.Edit) {
        edit.commit.forEach(::acceptCommittedChar)
    }

    fun removeCommittedCodePoint() {
        if (token.isNotEmpty()) token.deleteCharAt(token.lastIndex)
    }

    fun current(composing: String): String {
        if (composing.isEmpty()) return token.toString()
        return buildString(token.length + composing.length) {
            append(token)
            append(composing)
        }
    }

    /**
     * Returns the current token only when it can possibly match the local
     * candidate source. The length check happens before constructing a String so
     * long no-space input cannot cause an O(n) allocation on every key press.
     */
    fun currentForLookup(composing: String, maxLength: Int): String? {
        if (maxLength <= 0 || token.length + composing.length > maxLength) return null
        return current(composing)
    }

    fun clear() {
        token.setLength(0)
    }

    private fun acceptCommittedChar(ch: Char) {
        if (ch.isHangulSyllableOrJamo()) token.append(ch)
        else clear()
    }

    private fun Char.isHangulSyllableOrJamo(): Boolean =
        this in '\uAC00'..'\uD7A3' || this in '\u3131'..'\u318E'
}
