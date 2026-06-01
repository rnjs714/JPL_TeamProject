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

public class LoginPanel extends BasePanel {
    private final JTextField idField;
    private final JPasswordField passwordField;

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
        
        contentsPanel.add(centerPanel, BorderLayout.CENTER);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(event -> {
            // TODO: 빈 ID/비밀번호 검증 후 AuthController.login(...)을 호출한다.
            authController.login(idField.getText(), new String(passwordField.getPassword()));
        });
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(event -> {
            // TODO: 회원가입 입력값 검증 후 AuthController.register(...)를 호출한다.
            authController.register(idField.getText(), new String(passwordField.getPassword()));
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
