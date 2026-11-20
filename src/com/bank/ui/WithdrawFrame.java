package com.bank.ui;

import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.service.BankService;
import com.bank.ui.UITheme.*;
import com.bank.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class WithdrawFrame extends JFrame {
    private final String accountNumber;
    private final BankService bankService;
    private final DashboardFrame dashboard;
    private ModernTextField amountField;
    private ModernButton withdrawBtn;

    public WithdrawFrame(String accountNumber, BankService bankService, DashboardFrame dashboard) {
        this.accountNumber = accountNumber;
        this.bankService = bankService;
        this.dashboard = dashboard;
        initUI();
    }

    private void initUI() {
        setTitle("Withdraw Cash — Horizon Trust Bank");
        setSize(420, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(UITheme.BG_MAIN);

        CardPanel card = new CardPanel(UITheme.COLOR_DANGER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        card.setPreferredSize(new Dimension(360, 230));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("💸 Withdraw Cash");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_DANGER);
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

        // Footer
        JPanel footer = new JPanel(new GridLayout(1, 1));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        withdrawBtn = new ModernButton("Confirm Withdrawal", UITheme.COLOR_DANGER, UITheme.COLOR_DANGER_HOVER);
        withdrawBtn.setPreferredSize(new Dimension(140, 38));
        footer.add(withdrawBtn);
        card.add(footer, BorderLayout.SOUTH);

        background.add(card);
        add(background);

        withdrawBtn.addActionListener(e -> handleWithdraw());
        amountField.addActionListener(e -> handleWithdraw());
    }

    private void handleWithdraw() {
        String amountText = ValidationUtil.sanitize(amountField.getText());
        if (amountText.isEmpty()) {
            showError("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            ValidationUtil.validatePositiveAmount(amount, "Withdrawal amount");
        } catch (NumberFormatException ex) {
            showError("Please enter a valid numeric amount.");
            return;
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        setButtonEnabled(false);
        try {
            bankService.withdraw(accountNumber, amount);
            showSuccess("₹ " + String.format("%,.2f", amount) + " withdrawn successfully!");
            dashboard.refreshBalance();
            dispose();
        } catch (InsufficientFundsException ex) {
            showError("Insufficient funds! " + ex.getMessage());
        } catch (InvalidAmountException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Withdrawal failed: " + ex.getMessage());
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
        withdrawBtn.setEnabled(enabled);
    }
}