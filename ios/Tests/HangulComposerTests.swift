import XCTest

final class HangulComposerTests: XCTestCase {
    func testComposesSimpleSyllable() {
        let composer = HangulComposer()
        XCTAssertEqual(composer.input("ㄱ").composing, "ㄱ")
        XCTAssertEqual(composer.input("ㅏ").composing, "가")
    }

    func testMovesFinalConsonantToNextSyllable() {
        let composer = HangulComposer()
        _ = composer.input("ㄱ")
        _ = composer.input("ㅏ")
        XCTAssertEqual(composer.input("ㄴ").composing, "간")

        let edit = composer.input("ㅏ")
        XCTAssertEqual(edit.commit, "가")
        XCTAssertEqual(edit.composing, "나")
    }

    func testCompoundVowelAndBackspace() {
        let composer = HangulComposer()
        _ = composer.input("ㅇ")
        _ = composer.input("ㅗ")
        XCTAssertEqual(composer.input("ㅏ").composing, "와")
        XCTAssertEqual(composer.backspace().composing, "오")
    }

    func testCompoundFinalSplitsBeforeVowel() {
        let composer = HangulComposer()
        _ = composer.input("ㄷ")
        _ = composer.input("ㅏ")
        _ = composer.input("ㄹ")
        XCTAssertEqual(composer.input("ㄱ").composing, "닭")

        let edit = composer.input("ㅏ")
        XCTAssertEqual(edit.commit, "달")
        XCTAssertEqual(edit.composing, "가")
    }

    func testFlushClearsState() {
        let composer = HangulComposer()
        _ = composer.input("ㅎ")
        _ = composer.input("ㅏ")
        _ = composer.input("ㄴ")
        XCTAssertEqual(composer.flush(), "한")
        XCTAssertEqual(composer.currentText(), "")
        XCTAssertFalse(composer.backspace().consumed)
    }
}
