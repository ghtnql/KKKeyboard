import Foundation

/// Tracks only the current contiguous Hangul token for candidate lookup.
/// It never reads or persists the surrounding user message.
final class CandidateInputBuffer {
    private var token = ""

    func apply(_ edit: HangulEdit) {
        for character in edit.commit {
            if character.isHangulSyllableOrJamo {
                token.append(character)
            } else {
                clear()
            }
        }
    }

    func removeCommittedCharacter() {
        if !token.isEmpty { token.removeLast() }
    }

    func current(composing: String) -> String {
        token + composing
    }

    /// Avoid constructing a growing String on every key press once the current
    /// token is longer than any local candidate key can possibly match.
    func currentForLookup(composing: String, maxLength: Int) -> String? {
        guard maxLength > 0, token.count + composing.count <= maxLength else { return nil }
        return current(composing: composing)
    }

    func clear() {
        token.removeAll(keepingCapacity: true)
    }
}

private extension Character {
    var isHangulSyllableOrJamo: Bool {
        guard unicodeScalars.count == 1, let value = unicodeScalars.first?.value else { return false }
        return (0xAC00...0xD7A3).contains(value) || (0x3131...0x318E).contains(value)
    }
}
