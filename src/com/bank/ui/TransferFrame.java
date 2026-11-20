package com.bank.ui;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.service.BankService;
import com.bank.ui.UITheme.*;
import com.bank.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class TransferFrame extends JFrame {
    private final String fromAccountNumber;
    private final BankService bankService;
    private final DashboardFrame dashboard;
    private ModernTextField toAccountField, amountField;
    private ModernButton transferBtn;

    public TransferFrame(String fromAccountNumber, BankService bankService, DashboardFrame dashboard) {
        this.fromAccountNumber = fromAccountNumber;
        this.bankService = bankService;
        this.dashboard = dashboard;
        initUI();
    }

    private void initUI() {
        setTitle("Fund Transfer — Horizon Trust Bank");
        setSize(460, 390);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(UITheme.BG_MAIN);

        CardPanel card = new CardPanel(UITheme.COLOR_PURPLE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        card.setPreferredSize(new Dimension(400, 320));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("🔄 Instant Fund Transfer");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_PURPLE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("From Account: " + fromAccountNumber);
        subtitle.setFont(new Font("Consolas", Font.BOLD, 13));
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(15));
        card.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new GridLayout(4, 1, 0, 5));
        body.setOpaque(false);

        JLabel toLbl = new JLabel("Recipient Account Number");
        toLbl.setFont(UITheme.FONT_BOLD);
        toLbl.setForeground(UITheme.TEXT_DARK);
        toAccountField = new ModernTextField(16);

        JLabel amountLbl = new JLabel("Transfer Amount (₹)");
        amountLbl.setFont(UITheme.FONT_BOLD);
        amountLbl.setForeground(UITheme.TEXT_DARK);
        amountField = new ModernTextField(16);

        body.add(toLbl);
        body.add(toAccountField);
        body.add(amountLbl);
        body.add(amountField);
        card.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new GridLayout(1, 1));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        transferBtn = new ModernButton("Send Money Now", UITheme.COLOR_PURPLE, UITheme.COLOR_PURPLE_HOVER);
        transferBtn.setPreferredSize(new Dimension(150, 40));
        footer.add(transferBtn);
        card.add(footer, BorderLayout.SOUTH);

        background.add(card);
        add(background);

        transferBtn.addActionListener(e -> handleTransfer());
        amountField.addActionListener(e -> handleTransfer());
        toAccountField.addActionListener(e -> amountField.requestFocus());
    }

    private void handleTransfer() {
        String toAccount = ValidationUtil.sanitize(toAccountField.getText());
        String amountText = ValidationUtil.sanitize(amountField.getText());

        if (toAccount.isEmpty() || amountText.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        ValidationUtil.validateNotEqual(toAccount, fromAccountNumber, "Cannot transfer to the same account");
        ValidationUtil.validateAccountNumber(toAccount);

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            ValidationUtil.validatePositiveAmount(amount, "Transfer amount");
        } catch (NumberFormatException ex) {
            showError("Please enter a valid numeric amount.");
            return;
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        setButtonLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Exception error;
            @Override
            protected Void doInBackground() {
                try {
                    bankService.transfer(fromAccountNumber, toAccount, amount);
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                setButtonLoading(false);
                if (error != null) {
                    if (error instanceof AccountNotFoundException) {
                        showError("Recipient account not found: " + toAccount);
                    } else if (error instanceof InsufficientFundsException) {
                        showError("Insufficient funds for this transfer.");
                    } else if (error instanceof InvalidAmountException) {
                        showError(error.getMessage());
                    } else {
                        showError("Transfer failed: " + error.getMessage());
                    }
                } else {
                    showSuccess("₹ " + String.format("%,.2f", amount) + " transferred successfully to " + toAccount + "!");
                    dashboard.refreshBalance();
                    dispose();
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setButtonLoading(boolean loading) {
        transferBtn.setEnabled(!loading);
        transferBtn.setText(loading ? "Processing..." : "Send Money Now");
    }
}