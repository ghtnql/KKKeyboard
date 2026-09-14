import UIKit

final class ViewController: UIViewController {
    private let testField = UITextField()

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        configureOnboarding()
    }

    private func configureOnboarding() {
        let titleLabel = UILabel()
        titleLabel.font = .preferredFont(forTextStyle: .title1)
        titleLabel.adjustsFontForContentSizeCategory = true
        titleLabel.text = NSLocalizedString("onboarding.title", comment: "Onboarding title")

        let instructionLabel = UILabel()
        instructionLabel.numberOfLines = 0
        instructionLabel.font = .preferredFont(forTextStyle: .body)
        instructionLabel.adjustsFontForContentSizeCategory = true
        instructionLabel.text = NSLocalizedString("onboarding.instructions", comment: "Keyboard activation instructions")

        let settingsButton = UIButton(type: .system)
        settingsButton.configuration = .filled()
        settingsButton.configuration?.title = NSLocalizedString(
            "onboarding.settings_button",
            comment: "Open system settings button"
        )
        settingsButton.addTarget(self, action: #selector(openSettings), for: .touchUpInside)

        testField.borderStyle = .roundedRect
        testField.placeholder = NSLocalizedString(
            "onboarding.test_placeholder",
            comment: "Keyboard test field placeholder"
        )
        testField.clearButtonMode = .whileEditing
        testField.autocorrectionType = .no
        testField.autocapitalizationType = .none
        testField.returnKeyType = .done
        testField.accessibilityLabel = NSLocalizedString(
            "onboarding.test_accessibility",
            comment: "Keyboard test field accessibility label"
        )
        testField.delegate = self

        let stack = UIStackView(arrangedSubviews: [titleLabel, instructionLabel, settingsButton, testField])
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.axis = .vertical
        stack.spacing = 20
        stack.setCustomSpacing(28, after: instructionLabel)

        let scrollView = UIScrollView()
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.alwaysBounceVertical = false
        scrollView.keyboardDismissMode = .interactive
        view.addSubview(scrollView)
        scrollView.addSubview(stack)

        let centeredWhenPossible = stack.centerYAnchor.constraint(equalTo: scrollView.frameLayoutGuide.centerYAnchor)
        centeredWhenPossible.priority = .defaultLow

        NSLayoutConstraint.activate([
            scrollView.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor),
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),

            stack.leadingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.leadingAnchor, constant: 24),
            stack.trailingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.trailingAnchor, constant: -24),
            stack.topAnchor.constraint(greaterThanOrEqualTo: scrollView.contentLayoutGuide.topAnchor, constant: 24),
            stack.bottomAnchor.constraint(lessThanOrEqualTo: scrollView.contentLayoutGuide.bottomAnchor, constant: -24),
            stack.widthAnchor.constraint(equalTo: scrollView.frameLayoutGuide.widthAnchor, constant: -48),
            centeredWhenPossible,
            testField.heightAnchor.constraint(greaterThanOrEqualToConstant: 44)
        ])
    }

    @objc private func openSettings() {
        guard let url = URL(string: UIApplication.openSettingsURLString),
              UIApplication.shared.canOpenURL(url) else {
            return
        }
        UIApplication.shared.open(url)
    }
}

extension ViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }
}
