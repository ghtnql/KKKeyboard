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

        // A content container closes the scroll view's vertical constraint chain.
        // Its minimum viewport height keeps short onboarding content centered,
        // while the stack's intrinsic height expands it and enables scrolling
        // for landscape or large Dynamic Type sizes.
        let contentView = UIView()
        contentView.translatesAutoresizingMaskIntoConstraints = false

        view.addSubview(scrollView)
        scrollView.addSubview(contentView)
        contentView.addSubview(stack)

        NSLayoutConstraint.activate([
            scrollView.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor),
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),

            contentView.leadingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.leadingAnchor),
            contentView.trailingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.trailingAnchor),
            contentView.topAnchor.constraint(equalTo: scrollView.contentLayoutGuide.topAnchor),
            contentView.bottomAnchor.constraint(equalTo: scrollView.contentLayoutGuide.bottomAnchor),
            contentView.widthAnchor.constraint(equalTo: scrollView.frameLayoutGuide.widthAnchor),
            contentView.heightAnchor.constraint(greaterThanOrEqualTo: scrollView.frameLayoutGuide.heightAnchor),

            stack.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 24),
            stack.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -24),
            stack.topAnchor.constraint(greaterThanOrEqualTo: contentView.topAnchor, constant: 24),
            stack.bottomAnchor.constraint(lessThanOrEqualTo: contentView.bottomAnchor, constant: -24),
            stack.centerYAnchor.constraint(equalTo: contentView.centerYAnchor),
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
