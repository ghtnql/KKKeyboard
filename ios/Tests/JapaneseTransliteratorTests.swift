import XCTest

final class JapaneseTransliteratorTests: XCTestCase {
    func testSeedFixturesMatchExpectedCandidates() {
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "아리가토"), ["ありがとう"])
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "곤니치와"), ["こんにちは"])
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "스시"), ["すし", "寿司"])
    }

    func testLongVowelSokuonNasalAndYouonFixtures() {
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "코히"), ["コーヒー"])
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "킷테"), ["きって"])
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "온나"), ["おんな"])
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "료코"), ["りょこう"])
    }

    func testNormalizationAndUnknownInput() {
        XCTAssertEqual(JapaneseTransliterator.candidates(for: "  아리가토\n"), ["ありがとう"])
        XCTAssertTrue(JapaneseTransliterator.candidates(for: "미등록").isEmpty)
        XCTAssertTrue(JapaneseTransliterator.candidates(for: "   ").isEmpty)
    }
}
