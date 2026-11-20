package com.bank.ui;

import com.bank.exception.InvalidAmountException;
import com.bank.service.BankService;
import com.bank.ui.UITheme.*;
import com.bank.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class DepositFrame extends JFrame {
    private final String accountNumber;
    private final BankService bankService;
    private final DashboardFrame dashboard;
    private ModernTextField amountField;
    private ModernButton depositBtn;

    public DepositFrame(String accountNumber, BankService bankService, DashboardFrame dashboard) {
        this.accountNumber = accountNumber;
        this.bankService = bankService;
        this.dashboard = dashboard;
        initUI();
    }

    private void initUI() {
        setTitle("Deposit Money — Horizon Trust Bank");
        setSize(420, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(UITheme.BG_MAIN);

        CardPanel card = new CardPanel(UITheme.COLOR_PRIMARY);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        card.setPreferredSize(new Dimension(360, 230));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("💰 Deposit Funds");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Account: " + accountNumber);
        subtitle.setFont(new Font("Consolas", Font.BOLD, 13));
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(15));
        card.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new GridLayout(2, 1, 0, 5));
        body.setOpaque(false);

        JLabel amountLbl = new JLabel("Enter Amount (₹)");
        amountLbl.setFont(UITheme.FONT_BOLD);
        amountLbl.setForeground(UITheme.TEXT_DARK);

        amountField = new ModernTextField(14);
        amountField.setFont(new Font("Segoe UI", Font.BOLD, 15));

        body.add(amountLbl);
        body.add(amountField);
        card.add(body, BorderLayout.CENTER);

        // Footer button
        JPanel footer = new JPanel(new GridLayout(1, 1));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        depositBtn = new ModernButton("Confirm Deposit", UITheme.COLOR_PRIMARY, UITheme.COLOR_PRIMARY_HOVER);
        depositBtn.setPreferredSize(new Dimension(140, 38));
        footer.add(depositBtn);
        card.add(footer, BorderLayout.SOUTH);

        background.add(card);
        add(background);

        depositBtn.addActionListener(e -> handleDeposit());
        amountField.addActionListener(e -> handleDeposit());
    }

    private void handleDeposit() {
        String amountText = ValidationUtil.sanitize(amountField.getText());
        if (amountText.isEmpty()) {
            showError("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            ValidationUtil.validatePositiveAmount(amount, "Deposit amount");
        } catch (NumberFormatException ex) {
            showError("Please enter a valid numeric amount.");
            return;
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        setButtonEnabled(false);
        try {
            bankService.deposit(accountNumber, amount);
            showSuccess("₹ " + String.format("%,.2f", amount) + " deposited successfully!");
            dashboard.refreshBalance();
            dispose();
        } catch (InvalidAmountException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Deposit failed: " + ex.getMessage());
        } finally {
            setButtonEnabled(true);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setButtonEnabled(boolean enabled) {
        depositBtn.setEnabled(enabled);
    }
}