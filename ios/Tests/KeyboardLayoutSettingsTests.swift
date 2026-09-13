import XCTest

final class KeyboardLayoutSettingsTests: XCTestCase {
    func testDefaultsAreStableForBothOrientations() {
        let defaults = isolatedDefaults()
        let settings = KeyboardLayoutSettings(defaults: defaults)

        XCTAssertEqual(settings.profile(for: .portrait), KeyboardLayoutProfile(height: 260, numberRowEnabled: false))
        XCTAssertEqual(settings.profile(for: .landscape), KeyboardLayoutProfile(height: 260, numberRowEnabled: false))
    }

    func testPortraitAndLandscapeProfilesAreIndependent() {
        let defaults = isolatedDefaults()
        let settings = KeyboardLayoutSettings(defaults: defaults)

        settings.setHeight(300, for: .portrait)
        settings.setNumberRowEnabled(true, for: .portrait)
        settings.setHeight(220, for: .landscape)
        settings.setNumberRowEnabled(false, for: .landscape)

        XCTAssertEqual(settings.profile(for: .portrait), KeyboardLayoutProfile(height: 300, numberRowEnabled: true))
        XCTAssertEqual(settings.profile(for: .landscape), KeyboardLayoutProfile(height: 220, numberRowEnabled: false))
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
