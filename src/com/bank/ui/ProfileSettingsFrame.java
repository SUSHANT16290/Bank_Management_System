package com.bank.ui;

import com.bank.model.Customer;
import com.bank.service.BankService;
import com.bank.ui.UITheme.*;
import com.bank.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class ProfileSettingsFrame extends JFrame {
    private final Customer customer;
    private final BankService bankService;
    private final DashboardFrame dashboard;

    private ModernTextField nameField;
    private ModernTextField phoneField;
    private ModernTextField emailField;

    private ModernPasswordField currentPassField;
    private ModernPasswordField newPassField;
    private ModernPasswordField confirmPassField;

    private ModernButton saveProfileBtn;
    private ModernButton changePassBtn;

    public ProfileSettingsFrame(Customer customer, BankService bankService, DashboardFrame dashboard) {
        this.customer = customer;
        this.bankService = bankService;
        this.dashboard = dashboard;
        initUI();
    }

    private void initUI() {
        setTitle("Profile & Security Settings — Horizon Trust Bank");
        setSize(520, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(UITheme.BG_MAIN);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("👤 Profile & Security Center");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_PRIMARY);

        JLabel subtitle = new JLabel("Manage personal contact information and account security");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 14));
        contentPanel.setOpaque(false);

        // Card 1: Profile Information
        CardPanel profileCard = new CardPanel();
        profileCard.setLayout(new BorderLayout(0, 8));
        profileCard.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JLabel profileCardTitle = new JLabel("Personal Information");
        profileCardTitle.setFont(UITheme.FONT_HEADER);
        profileCardTitle.setForeground(UITheme.COLOR_PRIMARY);
        profileCard.add(profileCardTitle, BorderLayout.NORTH);

        JPanel profileForm = new JPanel(new GridLayout(3, 2, 8, 8));
        profileForm.setOpaque(false);

        nameField = new ModernTextField(14);
        nameField.setText(customer.getName());

        phoneField = new ModernTextField(14);
        phoneField.setText(customer.getPhone());

        emailField = new ModernTextField(14);
        emailField.setText(customer.getEmail());
        emailField.setEditable(false);
        emailField.setBackground(new Color(241, 245, 249));

        addFormField(profileForm, "Full Name:", nameField);
        addFormField(profileForm, "Phone Number:", phoneField);
        addFormField(profileForm, "Email (Read-only):", emailField);
        profileCard.add(profileForm, BorderLayout.CENTER);

        saveProfileBtn = new ModernButton("Save Profile Details", UITheme.COLOR_PRIMARY, UITheme.COLOR_PRIMARY_HOVER);
        saveProfileBtn.setPreferredSize(new Dimension(160, 34));
        saveProfileBtn.addActionListener(e -> handleUpdateProfile());
        profileCard.add(saveProfileBtn, BorderLayout.SOUTH);

        contentPanel.add(profileCard);

        // Card 2: Security & Password
        CardPanel securityCard = new CardPanel();
        securityCard.setLayout(new BorderLayout(0, 8));
        securityCard.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JLabel secCardTitle = new JLabel("Change Account Password");
        secCardTitle.setFont(UITheme.FONT_HEADER);
        secCardTitle.setForeground(UITheme.COLOR_DANGER);
        securityCard.add(secCardTitle, BorderLayout.NORTH);

        JPanel secForm = new JPanel(new GridLayout(3, 2, 8, 8));
        secForm.setOpaque(false);

        currentPassField = new ModernPasswordField(14);
        newPassField = new ModernPasswordField(14);
        confirmPassField = new ModernPasswordField(14);

        addFormField(secForm, "Current Password:", currentPassField);
        addFormField(secForm, "New Password:", newPassField);
        addFormField(secForm, "Confirm New Password:", confirmPassField);
        securityCard.add(secForm, BorderLayout.CENTER);

        changePassBtn = new ModernButton("Update Password", UITheme.COLOR_DANGER, UITheme.COLOR_DANGER_HOVER);
        changePassBtn.setPreferredSize(new Dimension(160, 34));
        changePassBtn.addActionListener(e -> handleChangePassword());
        securityCard.add(changePassBtn, BorderLayout.SOUTH);

        contentPanel.add(securityCard);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void handleUpdateProfile() {
        String newName = ValidationUtil.sanitize(nameField.getText());
        String newPhone = ValidationUtil.sanitize(phoneField.getText());

        try {
            ValidationUtil.validateProfileUpdate(newName, newPhone);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        setProfileButtonLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Exception error;
            @Override
            protected Void doInBackground() {
                try {
                    bankService.updateProfile(customer.getId(), newName, newPhone);
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                setProfileButtonLoading(false);
                if (error != null) {
                    showError(error.getMessage());
                } else {
                    customer.setName(newName);
                    customer.setPhone(newPhone);
                    showSuccess("Profile updated successfully!");
                    if (dashboard != null) dashboard.setTitle("Horizon Trust Bank — " + newName);
                }
            }
        };
        worker.execute();
    }

    private void handleChangePassword() {
        String currPass = new String(currentPassField.getPassword());
        String newPass = new String(newPassField.getPassword());
        String confirmPass = new String(confirmPassField.getPassword());

        if (currPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showError("Please enter all password fields.");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            showError("New passwords do not match!");
            return;
        }

        try {
            ValidationUtil.validatePasswordChange(currPass, newPass);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        setPasswordButtonLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Exception error;
            @Override
            protected Void doInBackground() {
                try {
                    bankService.changePassword(customer.getId(), currPass, newPass);
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                setPasswordButtonLoading(false);
                if (error != null) {
                    showError(error.getMessage());
                } else {
                    showSuccess("Password changed successfully!\nPlease use your new password next time you sign in.");
                    currentPassField.setText("");
                    newPassField.setText("");
                    confirmPassField.setText("");
                }
            }
        };
        worker.execute();
    }

    private void addFormField(JPanel parent, String labelText, JComponent comp) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        parent.add(lbl);
        parent.add(comp);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setProfileButtonLoading(boolean loading) {
        saveProfileBtn.setEnabled(!loading);
        saveProfileBtn.setText(loading ? "Saving..." : "Save Profile Details");
    }

    private void setPasswordButtonLoading(boolean loading) {
        changePassBtn.setEnabled(!loading);
        changePassBtn.setText(loading ? "Updating..." : "Update Password");
    }
}