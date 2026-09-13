import XCTest

final class SymbolLayoutTests: XCTestCase {
    func testSymbolLayoutMatchesHangulCharacterButtonCount() {
        let hangulCount = TwoBeolsikLayout.characterRows.flatMap { $0 }.count + TwoBeolsikLayout.bottomRow.count
        XCTAssertEqual(SymbolLayout.flattened.count, hangulCount)
    }

    func testSymbolLayoutContainsDigitsAndCommonPunctuation() {
        XCTAssertEqual(SymbolLayout.characterRows[0], ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"])
        XCTAssertTrue(SymbolLayout.flattened.contains("@"))
        XCTAssertTrue(SymbolLayout.flattened.contains("?"))
        XCTAssertTrue(SymbolLayout.flattened.contains("!"))
    }
}
