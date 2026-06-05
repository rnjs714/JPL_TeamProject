package gui;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import controller.AuthController;

// 로그인 화면
public class LoginPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    private final JTextField idField;
    private final JPasswordField passwordField;

    // 입력 폼/버튼 구성
    public LoginPanel(AuthController authController) {
        super("Login");

        JPanel centerPanel = new JPanel(new GridBagLayout());
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        idField = new JTextField(16);
        passwordField = new JPasswordField(16);
        formPanel.add(new JLabel("ID"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Password"));
        formPanel.add(passwordField);
        centerPanel.add(formPanel);
        
        contentsPanel.add(centerPanel, BorderLayout.CENTER); // 로그인 폼 중앙 배치

        JButton loginButton = new JButton("Login"); // 로그인 버튼
        loginButton.addActionListener(event -> {
            authController.login(idField.getText(), new String(passwordField.getPassword()));
        });
        JButton registerButton = new JButton("Register"); // 회원가입 버튼
        registerButton.addActionListener(event -> {
            authController.register(idField.getText(), new String(passwordField.getPassword()));
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
