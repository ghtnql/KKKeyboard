import UIKit

final class KeyboardViewController: UIInputViewController {
    private let stack = UIStackView()

    override func viewDidLoad() {
        super.viewDidLoad()
        configureKeyboard()
    }

    private func configureKeyboard() {
        view.backgroundColor = .systemBackground
        stack.axis = .horizontal
        stack.spacing = 8
        stack.distribution = .fillEqually
        stack.translatesAutoresizingMaskIntoConstraints = false

        let inputButton = makeButton(title: "가", action: #selector(insertSampleSyllable))
        let nextButton = makeButton(title: "🌐", action: #selector(handleNextKeyboard))
        stack.addArrangedSubview(inputButton)
        stack.addArrangedSubview(nextButton)

        view.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 12),
            stack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -12),
            stack.topAnchor.constraint(equalTo: view.topAnchor, constant: 10),
            stack.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -10),
            stack.heightAnchor.constraint(greaterThanOrEqualToConstant: 44)
        ])
    }

    private func makeButton(title: String, action: Selector) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 20, weight: .medium)
        button.addTarget(self, action: action, for: .touchUpInside)
        return button
    }

    @objc private func insertSampleSyllable() {
        textDocumentProxy.insertText("가")
    }

    @objc private func handleNextKeyboard() {
        advanceToNextInputMode()
    }
}
