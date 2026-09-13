enum TwoBeolsikLayout {
    static let characterRows: [[String]] = [
        ["ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ"],
        ["ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ"]
    ]

    static let bottomRow: [String] = ["ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ"]

    private static let shiftedKeys: [String: String] = [
        "ㅂ": "ㅃ",
        "ㅈ": "ㅉ",
        "ㄷ": "ㄸ",
        "ㄱ": "ㄲ",
        "ㅅ": "ㅆ",
        "ㅐ": "ㅒ",
        "ㅔ": "ㅖ"
    ]

    static func label(for baseLabel: String, shifted: Bool) -> String {
        guard shifted else { return baseLabel }
        return shiftedKeys[baseLabel] ?? baseLabel
    }

    static func hasShiftVariant(_ baseLabel: String) -> Bool {
        shiftedKeys[baseLabel] != nil
    }
}
