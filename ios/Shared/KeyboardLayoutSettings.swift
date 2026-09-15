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
    static let minimumUsableRowHeight = 32
    static let verticalInsets = 16
    static let rowSpacing = 6

    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func profile(for orientation: KeyboardOrientation) -> KeyboardLayoutProfile {
        let requestedHeight = requestedHeight(for: orientation)

        let numberKey = numberRowKey(for: orientation)
        let numberRowEnabled = defaults.object(forKey: numberKey) == nil
            ? false
            : defaults.bool(forKey: numberKey)

        let cursorKey = cursorRowKey(for: orientation)
        let cursorRowEnabled = defaults.object(forKey: cursorKey) == nil
            ? false
            : defaults.bool(forKey: cursorKey)

        let height = Self.renderedHeight(
            requestedHeight: requestedHeight,
            numberRowEnabled: numberRowEnabled,
            cursorRowEnabled: cursorRowEnabled
        )
        return KeyboardLayoutProfile(
            height: height,
            numberRowEnabled: numberRowEnabled,
            cursorRowEnabled: cursorRowEnabled
        )
    }

    static func renderedHeight(
        requestedHeight: Int,
        numberRowEnabled: Bool,
        cursorRowEnabled: Bool,
        settingsRowVisible: Bool = false
    ) -> Int {
        // Candidate + two upper character rows + bottom character row + control row.
        let baseRows = 5
        let rowCount = baseRows
            + (numberRowEnabled ? 1 : 0)
            + (cursorRowEnabled ? 1 : 0)
            + (settingsRowVisible ? 1 : 0)
        let minimumHeight = verticalInsets
            + max(0, rowCount - 1) * rowSpacing
            + rowCount * minimumUsableRowHeight
        return max(requestedHeight, minimumHeight)
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

    func nextHeight(for orientation: KeyboardOrientation) -> Int {
        nextHeight(after: requestedHeight(for: orientation))
    }

    private func requestedHeight(for orientation: KeyboardOrientation) -> Int {
        let storedHeight = defaults.integer(forKey: heightKey(for: orientation))
        return Self.supportedHeights.contains(storedHeight) ? storedHeight : Self.defaultHeight
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
