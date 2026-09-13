import UIKit

final class KeyboardViewController: UIInputViewController {
    private let composer = HangulComposer()
    private let candidateInput = CandidateInputBuffer()
    private var renderedComposition = ""
    private var japaneseCandidateMode = false
    private var displayedCandidates: [String] = []

    private let keyboardStack = UIStackView()
    private let candidateRow = UIStackView()
    private var modeButton: UIButton?

    override func viewDidLoad() {
        super.viewDidLoad()
        configureKeyboard()
    }

    override func viewWillDisappear(_ animated: Bool) {
        commitPendingComposition()
        clearCandidateTracking()
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
        addCharacterRow(["ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ"])
        addCharacterRow(["ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ"])
        addCharacterRow(["ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ"])
        addControlRow()

        view.addSubview(keyboardStack)
        NSLayoutConstraint.activate([
            keyboardStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 6),
            keyboardStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -6),
            keyboardStack.topAnchor.constraint(equalTo: view.topAnchor, constant: 8),
            keyboardStack.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -8),
            keyboardStack.heightAnchor.constraint(greaterThanOrEqualToConstant: 180)
        ])
    }

    private func configureCandidateRow() {
        candidateRow.axis = .horizontal
        candidateRow.spacing = 4
        candidateRow.distribution = .fillEqually
        candidateRow.isHidden = true
    }

    private func addCharacterRow(_ characters: [Character]) {
        let row = makeRow()
        for character in characters {
            let button = makeButton(title: String(character), action: #selector(handleCharacter(_:)))
            button.accessibilityLabel = String(character)
            row.addArrangedSubview(button)
        }
        keyboardStack.addArrangedSubview(row)
    }

    private func addControlRow() {
        let row = makeRow()
        let mode = makeButton(title: "한", action: #selector(handleModeToggle))
        modeButton = mode
        row.addArrangedSubview(mode)
        row.addArrangedSubview(makeButton(title: "🌐", action: #selector(handleNextKeyboard)))
        row.addArrangedSubview(makeButton(title: "space", action: #selector(handleSpace)))
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

    @objc private func handleCharacter(_ sender: UIButton) {
        guard let title = sender.currentTitle, let character = title.first else { return }
        let edit = composer.input(character)
        candidateInput.apply(edit)
        apply(edit)
        refreshCandidates()
    }

    @objc private func handleSpace() {
        commitPendingComposition()
        clearCandidateTracking()
        textDocumentProxy.insertText(" ")
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
        // Treat a mode switch as an input boundary so a Korean token can never
        // leak into a later Japanese lookup (or the reverse).
        commitPendingComposition()
        clearCandidateTracking()
        japaneseCandidateMode.toggle()
        modeButton?.setTitle(japaneseCandidateMode ? "日" : "한", for: .normal)
    }

    @objc private func handleCandidate(_ sender: UIButton) {
        guard let candidate = sender.currentTitle else { return }
        selectCandidate(candidate)
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
        if japaneseCandidateMode {
            candidates = JapaneseTransliterator.candidates(
                for: candidateInput.current(composing: composer.currentText())
            )
        } else {
            candidates = []
        }

        if !force && candidates == displayedCandidates { return }
        displayedCandidates = candidates
        candidateRow.arrangedSubviews.forEach {
            candidateRow.removeArrangedSubview($0)
            $0.removeFromSuperview()
        }

        guard !candidates.isEmpty else {
            candidateRow.isHidden = true
            return
        }

        for candidate in candidates.prefix(3) {
            candidateRow.addArrangedSubview(makeButton(title: candidate, action: #selector(handleCandidate(_:))))
        }
        candidateRow.isHidden = false
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
        candidateRow.arrangedSubviews.forEach {
            candidateRow.removeArrangedSubview($0)
            $0.removeFromSuperview()
        }
        candidateRow.isHidden = true
    }
}
