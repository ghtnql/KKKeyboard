import XCTest

final class JapaneseTransliteratorTests: XCTestCase {
    func testSeedFixturesMatchExpectedCandidates() {
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "아리가토"), ["ありがとう"])
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "곤니치와"), ["こんにちは"])
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "스시"), ["すし", "寿司"])
    }

    func testNormalizationAndUnknownInput() {
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "  아리가토\n"), ["ありがとう"])
        XCTAssertTrue(JapaneseTransliterator.candidates(for: "미등록").isEmpty)
        XCTAssertTrue(JapaneseTransliterator.candidates(for: "   ").isEmpty)
    }
}
