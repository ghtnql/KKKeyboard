import Foundation

struct HangulEdit: Equatable {
    let commit: String
    let composing: String?
    let consumed: Bool

    init(commit: String = "", composing: String? = nil, consumed: Bool = true) {
        self.commit = commit
        self.composing = composing
        self.consumed = consumed
    }
}

final class HangulComposer {
    private var initial = -1
    private var medial = -1
    private var finalIndex = -1

    func input(_ character: Character) -> HangulEdit {
        if let index = Self.initials.firstIndex(of: character) {
            return inputConsonant(character, newInitial: index)
        }
        if let index = Self.vowels.firstIndex(of: character) {
            return inputVowel(character, newMedial: index)
        }

        let pending = flush()
        return HangulEdit(commit: pending + String(character))
    }

    func backspace() -> HangulEdit {
        if finalIndex > 0 {
            let currentFinal = Self.finals[finalIndex]
            if let split = Self.compoundFinalSplit[currentFinal],
               let baseIndex = Self.finals.firstIndex(of: split.0) {
                finalIndex = baseIndex
            } else {
                finalIndex = -1
            }
        } else if medial >= 0 {
            let currentMedial = Self.vowels[medial]
            if let base = Self.compoundMedialBase[currentMedial],
               let baseIndex = Self.vowels.firstIndex(of: base) {
                medial = baseIndex
            } else {
                medial = -1
            }
        } else if initial >= 0 {
            initial = -1
        } else {
            return HangulEdit(consumed: false)
        }

        let text = currentText()
        return HangulEdit(composing: text.isEmpty ? nil : text)
    }

    func flush() -> String {
        let text = currentText()
        reset()
        return text
    }

    func reset() {
        initial = -1
        medial = -1
        finalIndex = -1
    }

    func currentText() -> String {
        if initial < 0 {
            return medial >= 0 ? String(Self.vowels[medial]) : ""
        }
        if medial < 0 {
            return String(Self.initials[initial])
        }

        let jong = finalIndex > 0 ? finalIndex : 0
        let codePoint = Self.hangulBase + ((initial * 21 + medial) * 28) + jong
        guard let scalar = UnicodeScalar(codePoint) else { return "" }
        return String(Character(scalar))
    }

    private func inputConsonant(_ character: Character, newInitial: Int) -> HangulEdit {
        if initial < 0 {
            if medial >= 0 {
                let commit = currentText()
                medial = -1
                initial = newInitial
                return HangulEdit(commit: commit, composing: currentText())
            }
            initial = newInitial
            return HangulEdit(composing: currentText())
        }

        if medial < 0 {
            let commit = currentText()
            initial = newInitial
            return HangulEdit(commit: commit, composing: currentText())
        }

        if finalIndex < 0,
           let candidateFinal = Self.finals.firstIndex(of: character),
           candidateFinal > 0 {
            finalIndex = candidateFinal
            return HangulEdit(composing: currentText())
        }

        if finalIndex > 0 {
            let currentFinal = Self.finals[finalIndex]
            if let compound = Self.compoundFinals[Pair(currentFinal, character)],
               let compoundIndex = Self.finals.firstIndex(of: compound) {
                finalIndex = compoundIndex
                return HangulEdit(composing: currentText())
            }
        }

        let commit = currentText()
        initial = newInitial
        medial = -1
        finalIndex = -1
        return HangulEdit(commit: commit, composing: currentText())
    }

    private func inputVowel(_ character: Character, newMedial: Int) -> HangulEdit {
        if initial < 0 {
            if medial < 0 {
                medial = newMedial
                return HangulEdit(composing: currentText())
            }

            let current = Self.vowels[medial]
            if let compound = Self.compoundMedials[Pair(current, character)],
               let compoundIndex = Self.vowels.firstIndex(of: compound) {
                medial = compoundIndex
                return HangulEdit(composing: currentText())
            }

            let commit = currentText()
            medial = newMedial
            return HangulEdit(commit: commit, composing: currentText())
        }

        if medial < 0 {
            medial = newMedial
            return HangulEdit(composing: currentText())
        }

        if finalIndex > 0 {
            let finalCharacter = Self.finals[finalIndex]
            if let split = Self.compoundFinalSplit[finalCharacter] {
                finalIndex = Self.finals.firstIndex(of: split.0) ?? -1
                let commit = currentText()
                initial = Self.initials.firstIndex(of: split.1) ?? -1
                medial = newMedial
                finalIndex = -1
                return HangulEdit(commit: commit, composing: currentText())
            }

            finalIndex = -1
            let commit = currentText()
            initial = Self.initials.firstIndex(of: finalCharacter) ?? -1
            medial = newMedial
            return HangulEdit(commit: commit, composing: currentText())
        }

        let current = Self.vowels[medial]
        if let compound = Self.compoundMedials[Pair(current, character)],
           let compoundIndex = Self.vowels.firstIndex(of: compound) {
            medial = compoundIndex
            return HangulEdit(composing: currentText())
        }

        let commit = currentText()
        initial = -1
        medial = newMedial
        finalIndex = -1
        return HangulEdit(commit: commit, composing: currentText())
    }

    private struct Pair: Hashable {
        let first: Character
        let second: Character

        init(_ first: Character, _ second: Character) {
            self.first = first
            self.second = second
        }
    }

    private static let hangulBase = 0xAC00

    private static let initials: [Character] = [
        "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ",
        "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    ]

    private static let vowels: [Character] = [
        "ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ",
        "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"
    ]

    private static let finals: [Character] = [
        "\0", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ",
        "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ",
        "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    ]

    private static let compoundMedials: [Pair: Character] = [
        Pair("ㅗ", "ㅏ"): "ㅘ", Pair("ㅗ", "ㅐ"): "ㅙ", Pair("ㅗ", "ㅣ"): "ㅚ",
        Pair("ㅜ", "ㅓ"): "ㅝ", Pair("ㅜ", "ㅔ"): "ㅞ", Pair("ㅜ", "ㅣ"): "ㅟ",
        Pair("ㅡ", "ㅣ"): "ㅢ"
    ]

    private static let compoundMedialBase: [Character: Character] = [
        "ㅘ": "ㅗ", "ㅙ": "ㅗ", "ㅚ": "ㅗ", "ㅝ": "ㅜ", "ㅞ": "ㅜ",
        "ㅟ": "ㅜ", "ㅢ": "ㅡ"
    ]

    private static let compoundFinals: [Pair: Character] = [
        Pair("ㄱ", "ㅅ"): "ㄳ", Pair("ㄴ", "ㅈ"): "ㄵ", Pair("ㄴ", "ㅎ"): "ㄶ",
        Pair("ㄹ", "ㄱ"): "ㄺ", Pair("ㄹ", "ㅁ"): "ㄻ", Pair("ㄹ", "ㅂ"): "ㄼ",
        Pair("ㄹ", "ㅅ"): "ㄽ", Pair("ㄹ", "ㅌ"): "ㄾ", Pair("ㄹ", "ㅍ"): "ㄿ",
        Pair("ㄹ", "ㅎ"): "ㅀ", Pair("ㅂ", "ㅅ"): "ㅄ"
    ]

    private static let compoundFinalSplit: [Character: (Character, Character)] = [
        "ㄳ": ("ㄱ", "ㅅ"), "ㄵ": ("ㄴ", "ㅈ"), "ㄶ": ("ㄴ", "ㅎ"),
        "ㄺ": ("ㄹ", "ㄱ"), "ㄻ": ("ㄹ", "ㅁ"), "ㄼ": ("ㄹ", "ㅂ"),
        "ㄽ": ("ㄹ", "ㅅ"), "ㄾ": ("ㄹ", "ㅌ"), "ㄿ": ("ㄹ", "ㅍ"),
        "ㅀ": ("ㄹ", "ㅎ"), "ㅄ": ("ㅂ", "ㅅ")
    ]
}
