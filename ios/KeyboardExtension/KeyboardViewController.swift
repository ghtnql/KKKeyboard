import UIKit

final class KeyboardViewController: UIInputViewController {
    private let composer = HangulComposer()
    private var renderedComposition = ""

    private let keyboardStack = UIStackView()

    override func viewDidLoad() {
        super.viewDidLoad()
        configureKeyboard()
    }

    override func viewWillDisappear(_ animated: Bool) {
        commitPendingComposition()
        super.viewWillDisappear(animated)
    }

    private func configureKeyboard() {
        view.backgroundColor = .systemBackground
        keyboardStack.axis = .vertical
        keyboardStack.spacing = 6
        keyboardStack.distribution = .fillEqually
        keyboardStack.translatesAutoresizingMaskIntoConstraints = false

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
        apply(composer.input(character))
    }

    @objc private func handleSpace() {
        replaceRenderedComposition(with: composer.flush())
        textDocumentProxy.insertText(" ")
    }

    @objc private func handleBackspace() {
        let edit = composer.backspace()
        if edit.consumed {
            apply(edit)
        } else {
            textDocumentProxy.deleteBackward()
        }
    }

    @objc private func handleNextKeyboard() {
        commitPendingComposition()
        advanceToNextInputMode()
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
}
