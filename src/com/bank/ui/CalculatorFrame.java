package com.bank.ui;

import com.bank.model.Account;
import com.bank.service.BankService;
import com.bank.ui.UITheme.*;
import com.bank.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class CalculatorFrame extends JFrame {
    private final BankService bankService;
    private final Account userAccount;
    private final DashboardFrame dashboard;

    public CalculatorFrame(BankService bankService, Account userAccount, DashboardFrame dashboard) {
        this.bankService = bankService;
        this.userAccount = userAccount;
        this.dashboard = dashboard;
        initUI();
    }

    private void initUI() {
        setTitle("Banking Financial Calculators — Horizon Trust Bank");
        setSize(560, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(UITheme.BG_MAIN);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 18, 18));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("📊 Financial Calculators");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_PRIMARY);

        JLabel subtitle = new JLabel("Compound Interest (Math.pow) & Loan Amortization");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // JTabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.addTab("📈 Fixed Deposit (FD)", createFDPanel());
        tabbedPane.addTab("💳 Loan EMI Calculator", createLoanPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    // ─── Fixed Deposit Calculator Panel ─────────────────────────────────────────
    private JPanel createFDPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setOpaque(false);

        ModernTextField amountField = new ModernTextField(14);
        amountField.setText("50000");

        JComboBox<String> tenureBox = new JComboBox<>(new String[]{"1 Year", "2 Years", "3 Years", "5 Years", "10 Years"});
        tenureBox.setFont(UITheme.FONT_REGULAR);

        ModernTextField rateField = new ModernTextField(14);
        rateField.setText("7.0");
        rateField.setEditable(false);

        tenureBox.addActionListener(e -> {
            int idx = tenureBox.getSelectedIndex();
            double[] rates = {6.5, 7.0, 7.25, 7.5, 7.75};
            rateField.setText(String.valueOf(rates[idx]));
        });

        addFormField(form, "Investment Amount (₹):", amountField);
        addFormField(form, "Tenure:", tenureBox);
        addFormField(form, "Interest Rate (% p.a.):", rateField);
        panel.add(form, BorderLayout.NORTH);

        // Results Card
        CardPanel resultCard = new CardPanel();
        resultCard.setLayout(new GridLayout(3, 2, 8, 8));
        resultCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel resInvested = new JLabel("₹ 50,000");
        JLabel resInterest = new JLabel("₹ 3,592");
        JLabel resMaturity = new JLabel("₹ 53,592");
        resMaturity.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resMaturity.setForeground(UITheme.COLOR_SUCCESS);

        addResultRow(resultCard, "Total Invested:", resInvested);
        addResultRow(resultCard, "Estimated Interest:", resInterest);
        addResultRow(resultCard, "Total Maturity Amount:", resMaturity);

        panel.add(resultCard, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionPanel.setOpaque(false);

        ModernButton calcBtn = new ModernButton("Calculate Returns", UITheme.COLOR_PRIMARY, UITheme.COLOR_PRIMARY_HOVER);
        ModernButton bookBtn = new ModernButton("Book this FD", UITheme.COLOR_SUCCESS, UITheme.COLOR_SUCCESS_HOVER);

        calcBtn.addActionListener(e -> handleFDCalculation(amountField, tenureBox, rateField, resInvested, resInterest, resMaturity));

        bookBtn.addActionListener(e -> handleFDBooking(amountField, tenureBox, rateField));

        actionPanel.add(calcBtn);
        actionPanel.add(bookBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void handleFDCalculation(ModernTextField amountField, JComboBox<String> tenureBox,
                                     ModernTextField rateField, JLabel resInvested, JLabel resInterest, JLabel resMaturity) {
        try {
            String amountText = ValidationUtil.sanitize(amountField.getText());
            if (amountText.isEmpty()) {
                showError("Please enter an investment amount.");
                return;
            }

            double p = Double.parseDouble(amountText);
            ValidationUtil.validatePositiveAmount(p, "Investment amount");

            int tenure = Integer.parseInt(((String) tenureBox.getSelectedItem()).split(" ")[0]);
            ValidationUtil.validatePositiveInt(tenure, "Tenure");

            double r = Double.parseDouble(rateField.getText().trim());
            ValidationUtil.validatePositiveAmount(r, "Interest rate");

            double maturity = bankService.calculateFDMaturity(p, r, tenure);
            double interest = maturity - p;

            resInvested.setText(String.format("₹ %,.2f", p));
            resInterest.setText(String.format("₹ %,.2f", interest));
            resMaturity.setText(String.format("₹ %,.2f", maturity));
        } catch (NumberFormatException ex) {
            showError("Please enter valid numeric values.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Calculation failed: " + ex.getMessage());
        }
    }

    private void handleFDBooking(ModernTextField amountField, JComboBox<String> tenureBox, ModernTextField rateField) {
        if (userAccount == null) {
            showError("Please log in to book a Fixed Deposit.");
            return;
        }
        try {
            String amountText = ValidationUtil.sanitize(amountField.getText());
            if (amountText.isEmpty()) {
                showError("Please enter an investment amount.");
                return;
            }

            double p = Double.parseDouble(amountText);
            ValidationUtil.validatePositiveAmount(p, "Investment amount");

            int tenure = Integer.parseInt(((String) tenureBox.getSelectedItem()).split(" ")[0]);
            ValidationUtil.validatePositiveInt(tenure, "Tenure");

            double r = Double.parseDouble(rateField.getText().trim());
            ValidationUtil.validatePositiveAmount(r, "Interest rate");

            int confirm = JOptionPane.showConfirmDialog(this,
                "Deduct ₹ " + String.format("%,.2f", p) + " from account " + userAccount.getAccountNumber() +
                " to book a " + tenure + "-year Fixed Deposit @ " + r + "%?",
                "Confirm FD Booking", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                bankService.bookFixedDeposit(userAccount.getAccountNumber(), p, tenure, r);
                showSuccess("🎉 Fixed Deposit booked successfully!\nFunds safely locked in high-interest deposit.");
                if (dashboard != null) dashboard.refreshBalance();
                dispose();
            }
        } catch (NumberFormatException ex) {
            showError("Please enter valid numeric values.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Booking failed: " + ex.getMessage());
        }
    }

    // ─── Loan EMI Calculator Panel ──────────────────────────────────────────────
    private JPanel createLoanPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setOpaque(false);

        ModernTextField loanField = new ModernTextField(14);
        loanField.setText("500000");

        ModernTextField rateField = new ModernTextField(14);
        rateField.setText("8.5");

        JComboBox<String> tenureBox = new JComboBox<>(new String[]{"1 Year", "2 Years", "3 Years", "5 Years", "7 Years", "10 Years", "15 Years", "20 Years"});
        tenureBox.setFont(UITheme.FONT_REGULAR);
        tenureBox.setSelectedIndex(3); // 5 Years

        addFormField(form, "Loan Amount (₹):", loanField);
        addFormField(form, "Interest Rate (% p.a.):", rateField);
        addFormField(form, "Tenure:", tenureBox);
        panel.add(form, BorderLayout.NORTH);

        // Results Card
        CardPanel resultCard = new CardPanel();
        resultCard.setLayout(new GridLayout(3, 2, 8, 8));
        resultCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel resEMI = new JLabel("₹ 10,258 / mo");
        resEMI.setFont(new Font("Segoe UI", Font.BOLD, 17));
        resEMI.setForeground(UITheme.COLOR_PURPLE);

        JLabel resInterest = new JLabel("₹ 1,15,487");
        JLabel resTotal = new JLabel("₹ 6,15,487");
        resTotal.setFont(UITheme.FONT_BOLD);

        addResultRow(resultCard, "Monthly EMI:", resEMI);
        addResultRow(resultCard, "Total Interest Payable:", resInterest);
        addResultRow(resultCard, "Total Payment (P + I):", resTotal);
        panel.add(resultCard, BorderLayout.CENTER);

        ModernButton calcBtn = new ModernButton("Calculate Loan EMI", UITheme.COLOR_PURPLE, UITheme.COLOR_PURPLE_HOVER);
        calcBtn.setPreferredSize(new Dimension(200, 38));

        calcBtn.addActionListener(e -> handleLoanCalculation(loanField, rateField, tenureBox, resEMI, resInterest, resTotal));

        panel.add(calcBtn, BorderLayout.SOUTH);
        return panel;
    }

    private void handleLoanCalculation(ModernTextField loanField, ModernTextField rateField, JComboBox<String> tenureBox,
                                       JLabel resEMI, JLabel resInterest, JLabel resTotal) {
        try {
            String loanText = ValidationUtil.sanitize(loanField.getText());
            if (loanText.isEmpty()) {
                showError("Please enter a loan amount.");
                return;
            }

            double principal = Double.parseDouble(loanText);
            ValidationUtil.validatePositiveAmount(principal, "Loan amount");

            String rateText = ValidationUtil.sanitize(rateField.getText());
            if (rateText.isEmpty()) {
                showError("Please enter an interest rate.");
                return;
            }

            double rate = Double.parseDouble(rateText);
            ValidationUtil.validatePositiveAmount(rate, "Interest rate");

            int years = Integer.parseInt(((String) tenureBox.getSelectedItem()).split(" ")[0]);
            ValidationUtil.validatePositiveInt(years, "Tenure");

            double emi = bankService.calculateLoanEMI(principal, rate, years);
            double totalPayment = emi * years * 12;
            double totalInterest = totalPayment - principal;

            resEMI.setText(String.format("₹ %,.2f / mo", emi));
            resInterest.setText(String.format("₹ %,.2f", totalInterest));
            resTotal.setText(String.format("₹ %,.2f", totalPayment));
        } catch (NumberFormatException ex) {
            showError("Please enter valid numeric values.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Calculation failed: " + ex.getMessage());
        }
    }

    private void addFormField(JPanel parent, String labelText, JComponent comp) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        parent.add(lbl);
        parent.add(comp);
    }

    private void addResultRow(JPanel parent, String labelText, JLabel valueLabel) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_REGULAR);
        lbl.setForeground(UITheme.TEXT_MUTED);
        parent.add(lbl);
        parent.add(valueLabel);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}