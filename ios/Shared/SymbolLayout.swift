enum SymbolLayout {
    static let characterRows: [[String]] = [
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"],
        ["-", "/", ":", ";", "(", ")", "₩", "&", "@"]
    ]

    static let bottomRow: [String] = [".", ",", "?", "!", "'", "\"", "#"]

    static var flattened: [String] {
        characterRows.flatMap { $0 } + bottomRow
    }
}
