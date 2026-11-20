package com.bank.ui;

import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.service.BankService;
import com.bank.ui.UITheme.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final Customer customer;
    private final BankService bankService;
    private JLabel balanceLabel;
    private JLabel accountNumberLabel;
    private JLabel accountTypeLabel;
    private Account currentAccount;
    private ModernButton refreshBtn;

    public DashboardFrame(Customer customer, BankService bankService) {
        this.customer = customer;
        this.bankService = bankService;
        loadAccounts();
        initUI();
    }

    private void loadAccounts() {
        List<Account> accounts = bankService.getAccounts(customer.getId());
        if (!accounts.isEmpty()) {
            currentAccount = accounts.get(0);
        }
    }

    private void initUI() {
        setTitle("Horizon Trust Bank — " + customer.getName());
        setSize(760, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(UITheme.BG_MAIN);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        // ── Top Navigation / Header ──────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BG_DARK_HEADER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLeft.setOpaque(false);

        JLabel logoLabel = new JLabel("🏛️");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel bankTitle = new JLabel("HORIZON TRUST BANK");
        bankTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bankTitle.setForeground(new Color(204, 251, 241)); // Mint accent #CCFBF1

        JLabel userLabel = new JLabel("•  " + customer.getName());
        userLabel.setFont(UITheme.FONT_REGULAR);
        userLabel.setForeground(new Color(226, 232, 240));

        headerLeft.add(logoLabel);
        headerLeft.add(bankTitle);
        headerLeft.add(userLabel);

        ModernButton logoutBtn = new ModernButton("Sign Out", UITheme.COLOR_DANGER, UITheme.COLOR_DANGER_HOVER);
        logoutBtn.setPreferredSize(new Dimension(96, 32));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(logoutBtn, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ── Center Account Card ──────────────────────────────────────────────
        CardPanel card = new CardPanel(UITheme.COLOR_PRIMARY);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        // Top row inside card: Title + Status Badge
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JLabel cardTitle = new JLabel("PRIMARY ACCOUNT SUMMARY");
        cardTitle.setFont(UITheme.FONT_SMALL);
        cardTitle.setForeground(UITheme.TEXT_MUTED);

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badgePanel.setOpaque(false);

        JLabel statusBadge = new JLabel("● ACTIVE");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadge.setForeground(UITheme.COLOR_SUCCESS);

        accountTypeLabel = new JLabel(currentAccount != null ? currentAccount.getAccountType() + " ACCOUNT" : "NO ACCOUNT");
        accountTypeLabel.setFont(UITheme.FONT_BOLD);
        accountTypeLabel.setForeground(UITheme.COLOR_PRIMARY);

        badgePanel.add(statusBadge);
        badgePanel.add(accountTypeLabel);

        cardHeader.add(cardTitle, BorderLayout.WEST);
        cardHeader.add(badgePanel, BorderLayout.EAST);
        card.add(cardHeader, BorderLayout.NORTH);

        // Center inside card: Account No + Large Balance
        JPanel cardBody = new JPanel(new GridLayout(2, 1, 0, 8));
        cardBody.setOpaque(false);
        cardBody.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));

        accountNumberLabel = new JLabel(currentAccount != null ? "Account Number:  " + currentAccount.getAccountNumber() : "No account found");
        accountNumberLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        accountNumberLabel.setForeground(UITheme.TEXT_DARK);

        balanceLabel = new JLabel(currentAccount != null ? "₹ " + String.format("%,.2f", currentAccount.getBalance()) : "₹ 0.00");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        balanceLabel.setForeground(UITheme.COLOR_PRIMARY);

        cardBody.add(accountNumberLabel);
        cardBody.add(balanceLabel);
        card.add(cardBody, BorderLayout.CENTER);

        mainPanel.add(card, BorderLayout.CENTER);

        // ── Action Buttons (2 x 4 Symmetrical Grid, Utility Bills Removed) ───
        JPanel buttonGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        buttonGrid.setOpaque(false);
        buttonGrid.setPreferredSize(new Dimension(720, 150));

        // Row 1: Core Banking Actions
        ModernButton depositBtn  = new ModernButton("💰 Deposit Funds", UITheme.COLOR_PRIMARY, UITheme.COLOR_PRIMARY_HOVER);
        ModernButton withdrawBtn = new ModernButton("💸 Withdraw Cash", UITheme.COLOR_DANGER, UITheme.COLOR_DANGER_HOVER);
        ModernButton transferBtn = new ModernButton("🔄 Fund Transfer", UITheme.COLOR_PURPLE, UITheme.COLOR_PURPLE_HOVER);
        ModernButton historyBtn  = new ModernButton("📋 Passbook History", UITheme.COLOR_BLUE, UITheme.COLOR_BLUE_HOVER);

        // Row 2: Management & Tools
        ModernButton calcBtn     = new ModernButton("📊 FD & Loan Calc", UITheme.COLOR_AMBER, UITheme.COLOR_AMBER_HOVER);
        ModernButton profileBtn  = new ModernButton("👤 Profile & Security", new Color(124, 58, 237), new Color(109, 40, 217));
        ModernButton exportBtn   = new ModernButton("📄 Export Statement", UITheme.COLOR_SECONDARY, UITheme.COLOR_SECONDARY_HOVER);
        refreshBtn               = new ModernButton("🔃 Refresh Balance", new Color(15, 23, 42), new Color(30, 41, 59));

        depositBtn.addActionListener(e -> handleAction("deposit"));
        withdrawBtn.addActionListener(e -> handleAction("withdraw"));
        transferBtn.addActionListener(e -> handleAction("transfer"));
        historyBtn.addActionListener(e -> handleAction("history"));
        calcBtn.addActionListener(e -> handleAction("calc"));
        profileBtn.addActionListener(e -> handleAction("profile"));
        exportBtn.addActionListener(e -> exportStatement());
        refreshBtn.addActionListener(e -> refreshBalance());

        buttonGrid.add(depositBtn);
        buttonGrid.add(withdrawBtn);
        buttonGrid.add(transferBtn);
        buttonGrid.add(historyBtn);
        buttonGrid.add(calcBtn);
        buttonGrid.add(profileBtn);
        buttonGrid.add(exportBtn);
        buttonGrid.add(refreshBtn);

        mainPanel.add(buttonGrid, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void handleAction(String action) {
        if (currentAccount == null) {
            JOptionPane.showMessageDialog(this, "No active bank account associated.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        switch (action) {
            case "deposit"  -> new DepositFrame(currentAccount.getAccountNumber(), bankService, this).setVisible(true);
            case "withdraw" -> new WithdrawFrame(currentAccount.getAccountNumber(), bankService, this).setVisible(true);
            case "transfer" -> new TransferFrame(currentAccount.getAccountNumber(), bankService, this).setVisible(true);
            case "calc"     -> new CalculatorFrame(bankService, currentAccount, this).setVisible(true);
            case "history"  -> new TransactionHistoryFrame(customer, bankService).setVisible(true);
            case "profile"  -> new ProfileSettingsFrame(customer, bankService, this).setVisible(true);
        }
    }

    public void refreshBalance() {
        if (refreshBtn != null) {
            refreshBtn.setEnabled(false);
            refreshBtn.setText("Loading...");
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                loadAccounts();
                return null;
            }

            @Override
            protected void done() {
                if (currentAccount != null) {
                    balanceLabel.setText("₹ " + String.format("%,.2f", currentAccount.getBalance()));
                    accountNumberLabel.setText("Account Number:  " + currentAccount.getAccountNumber());
                    accountTypeLabel.setText(currentAccount.getAccountType() + " ACCOUNT");
                }
                if (refreshBtn != null) {
                    refreshBtn.setEnabled(true);
                    refreshBtn.setText("🔃 Refresh Balance");
                }
            }
        };
        worker.execute();
    }

    private void exportStatement() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("statement_" + customer.getName().replace(" ", "_") + ".txt"));
        int result = fc.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                Exception error;
                @Override
                protected Void doInBackground() {
                    try {
                        var transactions = bankService.getTransactionHistory(customer.getId());
                        bankService.exportStatement(customer.getId(), customer.getName(), transactions,
                            fc.getSelectedFile().getAbsolutePath());
                    } catch (Exception ex) {
                        error = ex;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    if (error != null) {
                        JOptionPane.showMessageDialog(DashboardFrame.this,
                            "Failed to export statement: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(DashboardFrame.this,
                            "Statement exported successfully to:\n" + fc.getSelectedFile().getName(),
                            "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }
}
