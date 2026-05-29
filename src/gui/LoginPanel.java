package gui;

import controller.AuthController;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginPanel extends JPanel {
    private JTextField idField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private JLabel messageLabel;

    public LoginPanel(AuthController authController) {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        idField = new JTextField();
        passwordField = new JPasswordField();
        nameField = new JTextField();
        messageLabel = new JLabel(" ");

        formPanel.add(new JLabel("ID"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Password"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Name"));
        formPanel.add(nameField);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.addActionListener(event -> {
            // TODO: 빈 ID/비밀번호 검증 후 AuthController.login(...)을 호출한다.
            authController.login(idField.getText(), new String(passwordField.getPassword()));
        });

        registerButton.addActionListener(event -> {
            // TODO: 회원가입 입력값 검증 후 AuthController.register(...)를 호출한다.
            authController.register(idField.getText(), new String(passwordField.getPassword()), nameField.getText());
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(messageLabel, BorderLayout.NORTH);
    }

    public void showMessage(String message) {
        // TODO: 로그인/회원가입 성공 실패 메시지를 색상과 함께 표시한다.
        messageLabel.setText(message);
    }
}
