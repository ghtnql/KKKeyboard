import Foundation

enum KeyboardOrientation: String, CaseIterable {
    case portrait
    case landscape
}

struct KeyboardLayoutProfile: Equatable {
    let height: Int
    let numberRowEnabled: Bool
    let cursorRowEnabled: Bool
}

struct KeyboardLayoutSettings {
    static let supportedHeights = [220, 260, 300]
    static let defaultHeight = 260

    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func profile(for orientation: KeyboardOrientation) -> KeyboardLayoutProfile {
        let storedHeight = defaults.integer(forKey: heightKey(for: orientation))
        let height = Self.supportedHeights.contains(storedHeight) ? storedHeight : Self.defaultHeight

        let numberKey = numberRowKey(for: orientation)
        let numberRowEnabled = defaults.object(forKey: numberKey) == nil
            ? false
            : defaults.bool(forKey: numberKey)

        let cursorKey = cursorRowKey(for: orientation)
        let cursorRowEnabled = defaults.object(forKey: cursorKey) == nil
            ? false
            : defaults.bool(forKey: cursorKey)

        return KeyboardLayoutProfile(
            height: height,
            numberRowEnabled: numberRowEnabled,
            cursorRowEnabled: cursorRowEnabled
        )
    }

    func setHeight(_ height: Int, for orientation: KeyboardOrientation) {
        guard Self.supportedHeights.contains(height) else { return }
        defaults.set(height, forKey: heightKey(for: orientation))
    }

    func setNumberRowEnabled(_ enabled: Bool, for orientation: KeyboardOrientation) {
        defaults.set(enabled, forKey: numberRowKey(for: orientation))
    }

    func setCursorRowEnabled(_ enabled: Bool, for orientation: KeyboardOrientation) {
        defaults.set(enabled, forKey: cursorRowKey(for: orientation))
    }

    func nextHeight(after current: Int) -> Int {
        guard let index = Self.supportedHeights.firstIndex(of: current) else {
            return Self.defaultHeight
        }
        let nextIndex = Self.supportedHeights.index(after: index)
        return nextIndex == Self.supportedHeights.endIndex
            ? Self.supportedHeights[0]
            : Self.supportedHeights[nextIndex]
    }

    private func heightKey(for orientation: KeyboardOrientation) -> String {
        "layout.\(orientation.rawValue).height"
    }

    private func numberRowKey(for orientation: KeyboardOrientation) -> String {
        "layout.\(orientation.rawValue).numberRow"
    }

    private func cursorRowKey(for orientation: KeyboardOrientation) -> String {
        "layout.\(orientation.rawValue).cursorRow"
    }
}
