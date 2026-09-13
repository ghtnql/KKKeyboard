import UIKit

final class KeyboardViewController: UIInputViewController {
    private let composer = HangulComposer()
    private let candidateInput = CandidateInputBuffer()
    private let layoutSettings = KeyboardLayoutSettings()
    private var renderedComposition = ""
    private var japaneseCandidateMode = false
    private var displayedCandidates: [String] = []
    private var shiftEnabled = false
    private var symbolPage = false
    private var settingsVisible = false
    private var layoutOrientation: KeyboardOrientation = .portrait
    private var shiftedCharacterButtons: [(button: UIButton, baseLabel: String)] = []
    private var characterButtons: [UIButton] = []
    private var candidateButtons: [UIButton] = []

    private let keyboardStack = UIStackView()
    private let candidateRow = UIStackView()
    private let numberRow = UIStackView()
    private let settingsRow = UIStackView()
    private var modeButton: UIButton?
    private var shiftButton: UIButton?
    private var pageButton: UIButton?
    private var heightButton: UIButton?
    private var numberRowButton: UIButton?
    private var keyboardHeightConstraint: NSLayoutConstraint?

    private var hangulLabels: [String] {
        TwoBeolsikLayout.characterRows.flatMap { $0 } + TwoBeolsikLayout.bottomRow
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        configureKeyboard()
        applyLayoutProfile(for: currentOrientation())
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        applyLayoutProfile(for: currentOrientation())
    }

    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        coordinator.animate(alongsideTransition: nil) { [weak self] _ in
            guard let self else { return }
            self.applyLayoutProfile(for: self.currentOrientation())
        }
    }

    override func viewWillDisappear(_ animated: Bool) {
        commitPendingComposition()
        clearCandidateTracking()
        setShift(false)
        super.viewWillDisappear(animated)
    }

    private func configureKeyboard() {
        view.backgroundColor = .systemBackground
        keyboardStack.axis = .vertical
        keyboardStack.spacing = 6
        keyboardStack.distribution = .fillEqually
        keyboardStack.translatesAutoresizingMaskIntoConstraints = false

        configureCandidateRow()
        keyboardStack.addArrangedSubview(candidateRow)
        configureNumberRow()
        keyboardStack.addArrangedSubview(numberRow)
        TwoBeolsikLayout.characterRows.forEach(addCharacterRow)
        addBottomCharacterRow()
        addControlRow()
        configureSettingsRow()
        keyboardStack.addArrangedSubview(settingsRow)

        view.addSubview(keyboardStack)
        let heightConstraint = view.heightAnchor.constraint(equalToConstant: CGFloat(KeyboardLayoutSettings.defaultHeight))
        heightConstraint.priority = UILayoutPriority(999)
        keyboardHeightConstraint = heightConstraint
        NSLayoutConstraint.activate([
            keyboardStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 6),
            keyboardStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -6),
            keyboardStack.topAnchor.constraint(equalTo: view.topAnchor, constant: 8),
            keyboardStack.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -8),
            heightConstraint
        ])
    }

    private func configureCandidateRow() {
        candidateRow.axis = .horizontal
        candidateRow.spacing = 4
        candidateRow.distribution = .fillEqually
        candidateRow.isHidden = true

        candidateButtons = (0..<3).map { _ in
            let button = makeButton(title: "", action: #selector(handleCandidate(_:)))
            button.isHidden = true
            candidateRow.addArrangedSubview(button)
            return button
        }
    }

    private func configureNumberRow() {
        numberRow.axis = .horizontal
        numberRow.spacing = 4
        numberRow.distribution = .fillEqually
        numberRow.isHidden = true
        for digit in ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"] {
            numberRow.addArrangedSubview(makeButton(title: digit, action: #selector(handleDirectInput(_:))))
        }
    }

    private func configureSettingsRow() {
        settingsRow.axis = .horizontal
        settingsRow.spacing = 4
        settingsRow.distribution = .fillEqually
        settingsRow.isHidden = true

        let height = makeButton(title: "높이", action: #selector(handleHeightCycle))
        heightButton = height
        settingsRow.addArrangedSubview(height)

        let numbers = makeButton(title: "숫자열", action: #selector(handleNumberRowToggle))
        numberRowButton = numbers
        settingsRow.addArrangedSubview(numbers)

        settingsRow.addArrangedSubview(makeButton(title: "닫기", action: #selector(handleSettingsToggle)))
    }

    private func addCharacterRow(_ characters: [String]) {
        let row = makeRow()
        for character in characters {
            row.addArrangedSubview(makeCharacterButton(baseLabel: character))
        }
        keyboardStack.addArrangedSubview(row)
    }

    private func addBottomCharacterRow() {
        let row = makeRow()
        let shift = makeButton(title: "Shift", action: #selector(handleShift))
        shift.accessibilityLabel = "Shift"
        shiftButton = shift
        row.addArrangedSubview(shift)
        for character in TwoBeolsikLayout.bottomRow {
            row.addArrangedSubview(makeCharacterButton(baseLabel: character))
        }
        keyboardStack.addArrangedSubview(row)
    }

    private func addControlRow() {
        let row = makeRow()
        let mode = makeButton(title: "한", action: #selector(handleModeToggle))
        modeButton = mode
        row.addArrangedSubview(mode)
        row.addArrangedSubview(makeButton(title: "🌐", action: #selector(handleNextKeyboard)))
        let page = makeButton(title: "123", action: #selector(handlePageToggle))
        page.accessibilityLabel = "숫자 및 기호"
        page.accessibilityHint = "길게 누르면 레이아웃 설정"
        page.addGestureRecognizer(UILongPressGestureRecognizer(target: self, action: #selector(handlePageLongPress(_:))))
        pageButton = page
        row.addArrangedSubview(page)
        row.addArrangedSubview(makeButton(title: "space", action: #selector(handleSpace)))
        row.addArrangedSubview(makeButton(title: "return", action: #selector(handleReturn)))
        row.addArrangedSubview(makeButton(title: "⌫", action: #selector(handleBackspace)))
        keyboardStack.addArrangedSubview(row)
    }

    private func makeRow() -> UIStackView {
        let row = UIStackView()
        row.axis = .horizontal
        row.spacing = 4
        row.distribution = .fillEqually
        return row
    }

    private func makeButton(title: String, action: Selector) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 18, weight: .medium)
        button.backgroundColor = .secondarySystemBackground
        button.layer.cornerRadius = 6
        button.addTarget(self, action: action, for: .touchUpInside)
        return button
    }

    private func makeCharacterButton(baseLabel: String) -> UIButton {
        let button = makeButton(title: baseLabel, action: #selector(handleCharacter(_:)))
        button.accessibilityLabel = baseLabel
        characterButtons.append(button)
        if TwoBeolsikLayout.hasShiftVariant(baseLabel) {
            shiftedCharacterButtons.append((button, baseLabel))
        }
        return button
    }

    @objc private func handleCharacter(_ sender: UIButton) {
        guard let title = sender.currentTitle, !title.isEmpty else { return }

        if symbolPage {
            commitPendingComposition()
            clearCandidateTracking()
            textDocumentProxy.insertText(title)
            return
        }

        guard let character = title.first else { return }
        let edit = composer.input(character)
        candidateInput.apply(edit)
        apply(edit)
        refreshCandidates()
        if shiftEnabled { setShift(false) }
    }

    @objc private func handleDirectInput(_ sender: UIButton) {
        guard let title = sender.currentTitle, !title.isEmpty else { return }
        commitPendingComposition()
        clearCandidateTracking()
        textDocumentProxy.insertText(title)
    }

    @objc private func handleShift() {
        guard !symbolPage else { return }
        setShift(!shiftEnabled)
    }

    @objc private func handlePageToggle() {
        commitPendingComposition()
        clearCandidateTracking()
        setShift(false)
        setSymbolPage(!symbolPage)
    }

    @objc private func handlePageLongPress(_ recognizer: UILongPressGestureRecognizer) {
        guard recognizer.state == .began else { return }
        handleSettingsToggle()
    }

    @objc private func handleSettingsToggle() {
        settingsVisible.toggle()
        settingsRow.isHidden = !settingsVisible
    }

    @objc private func handleHeightCycle() {
        let profile = layoutSettings.profile(for: layoutOrientation)
        let nextHeight = layoutSettings.nextHeight(after: profile.height)
        layoutSettings.setHeight(nextHeight, for: layoutOrientation)
        applyLayoutProfile(for: layoutOrientation)
    }

    @objc private func handleNumberRowToggle() {
        let profile = layoutSettings.profile(for: layoutOrientation)
        layoutSettings.setNumberRowEnabled(!profile.numberRowEnabled, for: layoutOrientation)
        applyLayoutProfile(for: layoutOrientation)
    }

    @objc private func handleSpace() {
        commitPendingComposition()
        clearCandidateTracking()
        textDocumentProxy.insertText(" ")
    }

    @objc private func handleReturn() {
        commitPendingComposition()
        clearCandidateTracking()
        textDocumentProxy.insertText("\n")
    }

    @objc private func handleBackspace() {
        let edit = composer.backspace()
        if edit.consumed {
            apply(edit)
        } else {
            textDocumentProxy.deleteBackward()
            candidateInput.removeCommittedCharacter()
        }
        refreshCandidates()
    }

    @objc private func handleNextKeyboard() {
        commitPendingComposition()
        clearCandidateTracking()
        advanceToNextInputMode()
    }

    @objc private func handleModeToggle() {
        commitPendingComposition()
        clearCandidateTracking()
        japaneseCandidateMode.toggle()
        modeButton?.setTitle(japaneseCandidateMode ? "日" : "한", for: .normal)
    }

    @objc private func handleCandidate(_ sender: UIButton) {
        guard let candidate = sender.currentTitle, !candidate.isEmpty else { return }
        selectCandidate(candidate)
    }

    private func currentOrientation() -> KeyboardOrientation {
        if let interfaceOrientation = view.window?.windowScene?.interfaceOrientation {
            return interfaceOrientation.isLandscape ? .landscape : .portrait
        }
        return UIScreen.main.bounds.width > UIScreen.main.bounds.height ? .landscape : .portrait
    }

    private func applyLayoutProfile(for orientation: KeyboardOrientation) {
        layoutOrientation = orientation
        let profile = layoutSettings.profile(for: orientation)
        keyboardHeightConstraint?.constant = CGFloat(profile.height)
        numberRow.isHidden = !profile.numberRowEnabled
        heightButton?.setTitle("높이 \(profile.height)", for: .normal)
        numberRowButton?.setTitle(profile.numberRowEnabled ? "숫자열 켬" : "숫자열 끔", for: .normal)
    }

    private func setShift(_ enabled: Bool) {
        guard shiftEnabled != enabled else { return }
        shiftEnabled = enabled
        guard !symbolPage else {
            shiftButton?.isSelected = false
            shiftButton?.accessibilityValue = "off"
            return
        }
        for item in shiftedCharacterButtons {
            item.button.setTitle(
                TwoBeolsikLayout.label(for: item.baseLabel, shifted: enabled),
                for: .normal
            )
        }
        shiftButton?.isSelected = enabled
        shiftButton?.accessibilityValue = enabled ? "on" : "off"
    }

    private func setSymbolPage(_ enabled: Bool) {
        guard symbolPage != enabled else { return }
        symbolPage = enabled
        let labels = enabled ? SymbolLayout.flattened : hangulLabels
        guard labels.count == characterButtons.count else { return }

        for (button, label) in zip(characterButtons, labels) {
            button.setTitle(label, for: .normal)
            button.accessibilityLabel = label
        }
        shiftButton?.isHidden = enabled
        pageButton?.setTitle(enabled ? "가나다" : "123", for: .normal)
        pageButton?.accessibilityLabel = enabled ? "한글 자판" : "숫자 및 기호"
    }

    private func apply(_ edit: HangulEdit) {
        if !renderedComposition.isEmpty {
            textDocumentProxy.deleteBackward()
            renderedComposition = ""
        }

        if !edit.commit.isEmpty {
            textDocumentProxy.insertText(edit.commit)
        }

        if let composing = edit.composing, !composing.isEmpty {
            textDocumentProxy.insertText(composing)
            renderedComposition = composing
        }
    }

    private func refreshCandidates(force: Bool = false) {
        let candidates: [String]
        if japaneseCandidateMode && !symbolPage,
           let source = candidateInput.currentForLookup(
               composing: composer.currentText(),
               maxLength: JapaneseTransliterator.maxInputLength
           ) {
            candidates = JapaneseTransliterator.candidates(for: source)
        } else {
            candidates = []
        }

        if !force && candidates == displayedCandidates { return }
        displayedCandidates = candidates

        for (index, button) in candidateButtons.enumerated() {
            if index < candidates.count {
                button.setTitle(candidates[index], for: .normal)
                button.isHidden = false
            } else {
                button.setTitle("", for: .normal)
                button.isHidden = true
            }
        }
        candidateRow.isHidden = candidates.isEmpty
    }

    private func selectCandidate(_ candidate: String) {
        let source = candidateInput.current(composing: composer.currentText())
        guard !source.isEmpty else { return }

        commitPendingComposition()
        for _ in source {
            textDocumentProxy.deleteBackward()
        }
        textDocumentProxy.insertText(candidate)
        clearCandidateTracking()
    }

    private func replaceRenderedComposition(with text: String) {
        if !renderedComposition.isEmpty {
            textDocumentProxy.deleteBackward()
            renderedComposition = ""
        }
        if !text.isEmpty {
            textDocumentProxy.insertText(text)
        }
    }

    private func commitPendingComposition() {
        guard !renderedComposition.isEmpty else {
            composer.reset()
            return
        }
        replaceRenderedComposition(with: composer.flush())
    }

    private func clearCandidateTracking() {
        candidateInput.clear()
        displayedCandidates = []
        for button in candidateButtons {
            button.setTitle("", for: .normal)
            button.isHidden = true
        }
        candidateRow.isHidden = true
    }
}
