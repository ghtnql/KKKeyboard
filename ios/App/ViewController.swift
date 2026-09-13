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
        titleLabel.text = "ㅋㅋ키보드 시작하기"

        let instructionLabel = UILabel()
        instructionLabel.numberOfLines = 0
        instructionLabel.font = .preferredFont(forTextStyle: .body)
        instructionLabel.adjustsFontForContentSizeCategory = true
        instructionLabel.text = "1. 설정 > 일반 > 키보드 > 키보드에서 ‘새로운 키보드 추가’를 선택하세요.\n2. ㅋㅋ키보드를 추가하세요.\n3. 아래 입력칸을 눌러 🌐 키로 ㅋㅋ키보드를 선택하고 입력을 확인하세요.\n\n핵심 입력에는 전체 접근 허용이 필요하지 않습니다."

        let settingsButton = UIButton(type: .system)
        settingsButton.configuration = .filled()
        settingsButton.configuration?.title = "설정 열기"
        settingsButton.addTarget(self, action: #selector(openSettings), for: .touchUpInside)

        testField.borderStyle = .roundedRect
        testField.placeholder = "여기서 키보드 입력 테스트"
        testField.clearButtonMode = .whileEditing
        testField.autocorrectionType = .no
        testField.autocapitalizationType = .none
        testField.returnKeyType = .done
        testField.accessibilityLabel = "ㅋㅋ키보드 입력 테스트"
        testField.delegate = self

        let stack = UIStackView(arrangedSubviews: [titleLabel, instructionLabel, settingsButton, testField])
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.axis = .vertical
        stack.spacing = 20
        stack.setCustomSpacing(28, after: instructionLabel)

        view.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 24),
            stack.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: -24),
            stack.centerYAnchor.constraint(equalTo: view.safeAreaLayoutGuide.centerYAnchor),
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
