import XCTest

final class CandidateInputBufferTests: XCTestCase {
    func testTracksCommittedHangulAndCurrentComposition() {
        let buffer = CandidateInputBuffer()
        buffer.apply(HangulEdit(commit: "아리", composing: "가"))
        XCTAssertEqual(buffer.current(composing: "가"), "아리가")
    }

    func testNonHangulCommitClearsToken() {
        let buffer = CandidateInputBuffer()
        buffer.apply(HangulEdit(commit: "아리"))
        buffer.apply(HangulEdit(commit: " "))
        XCTAssertEqual(buffer.current(composing: ""), "")
    }

    func testBackspaceRemovesOnlyCommittedCharacter() {
        let buffer = CandidateInputBuffer()
        buffer.apply(HangulEdit(commit: "아리"))
        buffer.removeCommittedCharacter()
        XCTAssertEqual(buffer.current(composing: ""), "아")
    }

    func testUnsupportedJapaneseLookupLeavesTrackedHangulAvailableForContinuedInput() {
        let buffer = CandidateInputBuffer()
        buffer.apply(HangulEdit(commit: "미등록", composing: "입"))

        let sourceBeforeLookup = buffer.current(composing: "입")
        XCTAssertTrue(JapaneseTransliterator.candidates(for: sourceBeforeLookup).isEmpty)
        XCTAssertEqual(buffer.current(composing: "입"), "미등록입")

        buffer.apply(HangulEdit(commit: "입", composing: "력"))
        XCTAssertEqual(buffer.current(composing: "력"), "미등록입력")
    }
}
