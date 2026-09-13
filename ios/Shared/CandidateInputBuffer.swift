import Foundation

/// Tracks only the current contiguous Hangul token for candidate lookup.
/// It never reads or persists the surrounding user message.
final class CandidateInputBuffer {
    private var token = ""
    // Swift String.count is O(n). Track the committed Hangul character count so
    // candidate length rejection stays O(1) as the token grows during typing.
    private var tokenCharacterCount = 0

    func apply(_ edit: HangulEdit) {
        for character in edit.commit {
            if character.isHangulSyllableOrJamo {
                token.append(character)
                tokenCharacterCount += 1
            } else {
                clear()
            }
        }
    }

    func removeCommittedCharacter() {
        if !token.isEmpty {
            token.removeLast()
            tokenCharacterCount -= 1
        }
    }

    func current(composing: String) -> String {
        token + composing
    }

    /// Avoid constructing a growing String on every key press once the current
    /// token is longer than any local candidate key can possibly match.
    func currentForLookup(composing: String, maxLength: Int) -> String? {
        guard maxLength > 0, tokenCharacterCount + composing.count <= maxLength else { return nil }
        return current(composing: composing)
    }

    func clear() {
        token.removeAll(keepingCapacity: true)
        tokenCharacterCount = 0
    }
}

private extension Character {
    var isHangulSyllableOrJamo: Bool {
        guard unicodeScalars.count == 1, let value = unicodeScalars.first?.value else { return false }
        return (0xAC00...0xD7A3).contains(value) || (0x3131...0x318E).contains(value)
    }
}
