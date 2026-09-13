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
}
