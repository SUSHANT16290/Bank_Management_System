package com.bank.ui;

import com.bank.model.Customer;
import com.bank.service.BankService;
import com.bank.ui.UITheme.*;
import com.bank.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private ModernTextField nameField, emailField, phoneField;
    private ModernPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> accountTypeBox;
    private ModernButton registerBtn, backBtn;
    private BankService bankService;

    public RegisterFrame(BankService bankService) {
        this.bankService = bankService;
        initUI();
    }

    private void initUI() {
        setTitle("Open Account — Horizon Trust Bank");
        setSize(520, 570);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(UITheme.BG_MAIN);

        CardPanel card = new CardPanel(UITheme.COLOR_PRIMARY);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        card.setPreferredSize(new Dimension(460, 490));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel logo = new JLabel("🏛️", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Open a New Account");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Join Horizon Trust Bank digital network.");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(logo);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(subtitle);
        headerPanel.add(Box.createVerticalStrut(14));
        card.add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 8));
        form.setOpaque(false);

        nameField = new ModernTextField(15);
        emailField = new ModernTextField(15);
        phoneField = new ModernTextField(15);
        passwordField = new ModernPasswordField(15);
        confirmPasswordField = new ModernPasswordField(15);
        accountTypeBox = new JComboBox<>(new String[]{"SAVINGS", "CURRENT"});
        accountTypeBox.setFont(UITheme.FONT_REGULAR);
        accountTypeBox.setBackground(Color.WHITE);

        form.add(createLabel("Full Name:"));
        form.add(nameField);
        form.add(createLabel("Email Address:"));
        form.add(emailField);
        form.add(createLabel("Phone Number:"));
        form.add(phoneField);
        form.add(createLabel("Account Type:"));
        form.add(accountTypeBox);
        form.add(createLabel("Password:"));
        form.add(passwordField);
        form.add(createLabel("Confirm Password:"));
        form.add(confirmPasswordField);

        card.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        registerBtn = new ModernButton("Create Account", UITheme.COLOR_PRIMARY, UITheme.COLOR_PRIMARY_HOVER);
        registerBtn.setPreferredSize(new Dimension(140, 38));

        backBtn = new ModernButton("Back to Login", UITheme.COLOR_SECONDARY, UITheme.COLOR_SECONDARY_HOVER);
        backBtn.setPreferredSize(new Dimension(140, 38));

        btnPanel.add(registerBtn);
        btnPanel.add(backBtn);
        card.add(btnPanel, BorderLayout.SOUTH);

        background.add(card);
        add(background);

        registerBtn.addActionListener(e -> handleRegister());
        backBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.TEXT_DARK);
        return label;
    }

    private void handleRegister() {
        String name = ValidationUtil.sanitize(nameField.getText());
        String email = ValidationUtil.sanitize(emailField.getText());
        String phone = ValidationUtil.sanitize(phoneField.getText());
        String pass = new String(passwordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());
        String accountType = (String) accountTypeBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showError("Name, Email, and Password are required fields.");
            return;
        }

        if (!email.contains("@")) {
            showError("Please enter a valid email address.");
            return;
        }

        if (pass.length() < 4) {
            showError("Password must be at least 4 characters long.");
            return;
        }

        if (!pass.equals(confirmPass)) {
            showError("Passwords do not match!");
            return;
        }

        try {
            Customer customer = bankService.register(name, email, phone, pass, accountType);
            JOptionPane.showMessageDialog(this,
                "Account created successfully for Horizon Trust Bank!\nYou may now log in.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
        } catch (Exception ex) {
            showError("Registration failed: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Registration Error", JOptionPane.ERROR_MESSAGE);
    }
}
