package com.bank.ui;

import com.bank.model.Customer;
import com.bank.service.BankService;
import com.bank.ui.UITheme.*;
import com.bank.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {
    private ModernTextField emailField;
    private ModernPasswordField passwordField;
    private ModernButton loginBtn, registerBtn;
    private BankService bankService;

    public LoginFrame() {
        bankService = new BankService();
        initUI();
    }

    private void initUI() {
        setTitle("Horizon Trust Bank — Sign In");
        setSize(460, 430);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Background Panel
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(UITheme.BG_MAIN);

        // Center Card with top emerald stripe
        CardPanel card = new CardPanel(UITheme.COLOR_PRIMARY);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        card.setPreferredSize(new Dimension(390, 350));

        // Header Section
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("🏛️", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("HORIZON TRUST BANK");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.COLOR_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Secure Digital Banking Portal");
        subtitleLabel.setFont(UITheme.FONT_SMALL);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(logoLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalStrut(16));

        card.add(headerPanel, BorderLayout.NORTH);

        // Form Section
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        formPanel.setOpaque(false);

        JLabel emailLbl = new JLabel("Email Address");
        emailLbl.setFont(UITheme.FONT_BOLD);
        emailLbl.setForeground(UITheme.TEXT_DARK);
        emailField = new ModernTextField(20);

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(UITheme.FONT_BOLD);
        passLbl.setForeground(UITheme.TEXT_DARK);
        passwordField = new ModernPasswordField(20);

        formPanel.add(emailLbl);
        formPanel.add(emailField);
        formPanel.add(passLbl);
        formPanel.add(passwordField);

        card.add(formPanel, BorderLayout.CENTER);

        // Button Section
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        loginBtn = new ModernButton("Sign In", UITheme.COLOR_PRIMARY, UITheme.COLOR_PRIMARY_HOVER);
        loginBtn.setPreferredSize(new Dimension(130, 38));

        registerBtn = new ModernButton("Register", UITheme.COLOR_SECONDARY, UITheme.COLOR_SECONDARY_HOVER);
        registerBtn.setPreferredSize(new Dimension(130, 38));

        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);

        card.add(btnPanel, BorderLayout.SOUTH);

        background.add(card);
        add(background);

        // Action Listeners
        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> openRegister());

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });

        emailField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });
    }

    private void handleLogin() {
        String email = ValidationUtil.sanitize(emailField.getText());
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        if (!email.contains("@")) {
            showError("Please enter a valid email address.");
            return;
        }

        setButtonsEnabled(false);
        try {
            Customer customer = bankService.login(email, password);
            if (customer != null) {
                dispose();
                new DashboardFrame(customer, bankService).setVisible(true);
            } else {
                showError("Invalid email or password! Please check your credentials or register first.");
            }
        } catch (Exception ex) {
            showError("Login failed: " + ex.getMessage());
        } finally {
            setButtonsEnabled(true);
        }
    }

    private void openRegister() {
        dispose();
        new RegisterFrame(bankService).setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setButtonsEnabled(boolean enabled) {
        loginBtn.setEnabled(enabled);
        registerBtn.setEnabled(enabled);
    }
}
