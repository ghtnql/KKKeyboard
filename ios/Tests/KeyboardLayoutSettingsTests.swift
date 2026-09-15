import XCTest

final class KeyboardLayoutSettingsTests: XCTestCase {
    func testDefaultsAreStableForBothOrientations() {
        let defaults = isolatedDefaults()
        let settings = KeyboardLayoutSettings(defaults: defaults)

        XCTAssertEqual(
            settings.profile(for: .portrait),
            KeyboardLayoutProfile(height: 260, numberRowEnabled: false, cursorRowEnabled: false)
        )
        XCTAssertEqual(
            settings.profile(for: .landscape),
            KeyboardLayoutProfile(height: 260, numberRowEnabled: false, cursorRowEnabled: false)
        )
    }

    func testPortraitAndLandscapeProfilesAreIndependent() {
        let defaults = isolatedDefaults()
        let settings = KeyboardLayoutSettings(defaults: defaults)

        settings.setHeight(300, for: .portrait)
        settings.setNumberRowEnabled(true, for: .portrait)
        settings.setCursorRowEnabled(true, for: .portrait)
        settings.setHeight(220, for: .landscape)
        settings.setNumberRowEnabled(false, for: .landscape)
        settings.setCursorRowEnabled(false, for: .landscape)

        XCTAssertEqual(
            settings.profile(for: .portrait),
            KeyboardLayoutProfile(height: 314, numberRowEnabled: true, cursorRowEnabled: true)
        )
        XCTAssertEqual(
            settings.profile(for: .landscape),
            KeyboardLayoutProfile(height: 238, numberRowEnabled: false, cursorRowEnabled: false)
        )
    }

    func testRenderedHeightPreservesMinimumUsableRows() {
        XCTAssertEqual(
            KeyboardLayoutSettings.renderedHeight(
                requestedHeight: 220,
                numberRowEnabled: false,
                cursorRowEnabled: false
            ),
            238
        )
        XCTAssertEqual(
            KeyboardLayoutSettings.renderedHeight(
                requestedHeight: 220,
                numberRowEnabled: true,
                cursorRowEnabled: true
            ),
            314
        )
        XCTAssertEqual(
            KeyboardLayoutSettings.renderedHeight(
                requestedHeight: 300,
                numberRowEnabled: false,
                cursorRowEnabled: false
            ),
            300
        )
    }

    func testUnsupportedHeightIsIgnoredAndHeightCycleWraps() {
        let defaults = isolatedDefaults()
        let settings = KeyboardLayoutSettings(defaults: defaults)

        settings.setHeight(999, for: .portrait)
        XCTAssertEqual(settings.profile(for: .portrait).height, 260)
        XCTAssertEqual(settings.nextHeight(after: 220), 260)
        XCTAssertEqual(settings.nextHeight(after: 260), 300)
        XCTAssertEqual(settings.nextHeight(after: 300), 220)
        XCTAssertEqual(settings.nextHeight(after: 999), 260)
    }

    private func isolatedDefaults() -> UserDefaults {
        let suite = "KeyboardLayoutSettingsTests.\(UUID().uuidString)"
        let defaults = UserDefaults(suiteName: suite)!
        defaults.removePersistentDomain(forName: suite)
        return defaults
    }
}
