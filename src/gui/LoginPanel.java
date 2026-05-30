package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controller.AuthController;

public class LoginPanel extends JPanel {
    private final JTextField idField;
    private final JPasswordField passwordField;
    private final JLabel messageLabel;

    public LoginPanel(AuthController authController) {

        setLayout(new BorderLayout());


        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(50, 130, 50));
        titlePanel.add(new JLabel("Login", SwingConstants.CENTER), BorderLayout.CENTER);


        JPanel centerPanel = new JPanel(new GridBagLayout());
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        idField = new JTextField(16);
        passwordField = new JPasswordField(16);
        messageLabel = new JLabel(" ");

        formPanel.add(new JLabel("ID"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Password"));
        formPanel.add(passwordField);
        centerPanel.add(formPanel);

        JPanel contentsPanel = new JPanel(new BorderLayout());
        contentsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentsPanel.add(centerPanel, BorderLayout.CENTER);
        contentsPanel.add(messageLabel, BorderLayout.SOUTH);

        
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

        add(titlePanel, BorderLayout.NORTH);
        add(contentsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
