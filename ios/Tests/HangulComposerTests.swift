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

    func testAllCompoundVowelsCompose() {
        let cases: [(Character, Character, String)] = [
            ("ㅗ", "ㅏ", "와"), ("ㅗ", "ㅐ", "왜"), ("ㅗ", "ㅣ", "외"),
            ("ㅜ", "ㅓ", "워"), ("ㅜ", "ㅔ", "웨"), ("ㅜ", "ㅣ", "위"),
            ("ㅡ", "ㅣ", "의")
        ]

        for (first, second, expected) in cases {
            let composer = HangulComposer()
            _ = composer.input("ㅇ")
            _ = composer.input(first)
            XCTAssertEqual(composer.input(second).composing, expected)
        }
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

    func testAllCompoundFinalsComposeAndSplit() {
        let cases: [(Character, Character, String, String, String)] = [
            ("ㄱ", "ㅅ", "넋", "넉", "사"),
            ("ㄴ", "ㅈ", "앉", "안", "자"),
            ("ㄴ", "ㅎ", "많", "만", "하"),
            ("ㄹ", "ㄱ", "닭", "달", "가"),
            ("ㄹ", "ㅁ", "삶", "살", "마"),
            ("ㄹ", "ㅂ", "밟", "발", "바"),
            ("ㄹ", "ㅅ", "핥", "할", "사"),
            ("ㄹ", "ㅌ", "핥", "할", "타"),
            ("ㄹ", "ㅍ", "읊", "을", "파"),
            ("ㄹ", "ㅎ", "잃", "일", "하"),
            ("ㅂ", "ㅅ", "값", "갑", "사")
        ]

        for (firstFinal, secondFinal, expectedCompound, expectedCommit, expectedNext) in cases {
            let composer = HangulComposer()
            _ = composer.input(expectedCompound == "넋" ? "ㄴ" : expectedCompound == "앉" ? "ㅇ" : expectedCompound == "많" ? "ㅁ" : expectedCompound == "닭" ? "ㄷ" : expectedCompound == "삶" ? "ㅅ" : expectedCompound == "밟" ? "ㅂ" : expectedCompound == "핥" ? "ㅎ" : expectedCompound == "읊" ? "ㅇ" : expectedCompound == "잃" ? "ㅇ" : "ㄱ")
            _ = composer.input(expectedCompound == "넋" ? "ㅓ" : expectedCompound == "앉" ? "ㅏ" : expectedCompound == "많" ? "ㅏ" : expectedCompound == "닭" ? "ㅏ" : expectedCompound == "삶" ? "ㅏ" : expectedCompound == "밟" ? "ㅏ" : expectedCompound == "핥" ? "ㅏ" : expectedCompound == "읊" ? "ㅡ" : expectedCompound == "잃" ? "ㅣ" : "ㅏ")
            _ = composer.input(firstFinal)
            XCTAssertEqual(composer.input(secondFinal).composing, expectedCompound)

            let split = composer.input("ㅏ")
            XCTAssertEqual(split.commit, expectedCommit)
            XCTAssertEqual(split.composing, expectedNext)
        }
    }

    func testAllInitialAndMedialIndexesRemainMappedCorrectly() {
        let initials: [Character] = ["ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"]
        let expectedA: [String] = ["가", "까", "나", "다", "따", "라", "마", "바", "빠", "사", "싸", "아", "자", "짜", "차", "카", "타", "파", "하"]

        for (initial, expected) in zip(initials, expectedA) {
            let composer = HangulComposer()
            _ = composer.input(initial)
            XCTAssertEqual(composer.input("ㅏ").composing, expected)
        }

        let vowels: [Character] = ["ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"]
        for vowel in vowels {
            let composer = HangulComposer()
            XCTAssertEqual(composer.input(vowel).composing, String(vowel))
        }
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

    func testRepeatedFastCompositionDoesNotLoseOrLeakState() {
        let composer = HangulComposer()
        var output = ""
        output.reserveCapacity(2_000)

        for _ in 0..<2_000 {
            _ = composer.input("ㅎ")
            _ = composer.input("ㅏ")
            _ = composer.input("ㄴ")
            output += composer.flush()
            XCTAssertEqual(composer.currentText(), "")
        }

        XCTAssertEqual(output.count, 2_000)
        XCTAssertEqual(output, String(repeating: "한", count: 2_000))
    }
}
