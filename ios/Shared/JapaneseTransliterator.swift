import Foundation

/// Minimal deterministic Hangul-pronunciation -> Japanese candidate provider.
///
/// Keep candidate lookup local and allocation-light in the keyboard hot path.
/// The seed table mirrors shared/test-fixtures/ja_transliteration.json and can
/// later be replaced by the shared rule/dictionary pipeline without changing
/// the candidate UI contract.
enum JapaneseTransliterator {
    private static let seedCandidates: [String: [String]] = [
        "아리가토": ["ありがとう"],
        "곤니치와": ["こんにちは"],
        "스시": ["すし", "寿司"]
    ]

    static func candidates(for inputHangul: String) -> [String] {
        let normalized = inputHangul.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalized.isEmpty else { return [] }
        return seedCandidates[normalized] ?? []
    }
}
