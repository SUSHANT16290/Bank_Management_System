package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.CustomerDAO;
import com.bank.dao.TransactionDAO;
import com.bank.db.DBConnection;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.Account;
import com.bank.model.CurrentAccount;
import com.bank.model.Customer;
import com.bank.model.SavingsAccount;
import com.bank.model.Transaction;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * BankService - Business Logic Layer
 * Integrates OOP models, multithreaded operations, and JDBC DAOs.
 */
public class BankService {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    // ─── Customer Authentication & Registration ─────────────────────────────

    public Customer register(String name, String email, String phone, String password, String accountType) throws Exception {
        if (name == null || name.trim().length() < 2) {
            throw new Exception("Name must be at least 2 characters.");
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new Exception("Please enter a valid email address.");
        }
        if (password == null || password.length() < 4) {
            throw new Exception("Password must be at least 4 characters.");
        }

        try {
            if (customerDAO.emailExists(email)) {
                throw new Exception("An account is already registered with email: " + email);
            }

            Customer customer = new Customer(name.trim(), email.trim(), phone != null ? phone.trim() : "", password);
            boolean saved = customerDAO.registerCustomer(customer);
            if (!saved) {
                throw new Exception("Failed to register customer into database.");
            }

            // Auto-create initial account for customer (Module 2: Polymorphism)
            String accNumber = generateAccountNumber();
            Account account;
            if ("CURRENT".equalsIgnoreCase(accountType)) {
                account = new CurrentAccount(accNumber, customer.getId(), 1000.0);
            } else {
                account = new SavingsAccount(accNumber, customer.getId(), 500.0);
            }
            accountDAO.createAccount(account);
            return customer;
        } catch (SQLException e) {
            throw new Exception("Database error during registration: " + e.getMessage(), e);
        }
    }

    public Customer login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        try {
            return customerDAO.loginCustomer(email.trim(), password);
        } catch (SQLException e) {
            throw new Exception("Database connection error: " + e.getMessage(), e);
        }
    }

    // ─── Account Operations ─────────────────────────────────────────────────

    public List<Account> getAccounts(int customerId) {
        try {
            return accountDAO.getAccountsByCustomer(customerId);
        } catch (SQLException e) {
            System.err.println("Error fetching accounts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Account getAccount(String accountNumber) throws AccountNotFoundException, Exception {
        try {
            Account acc = accountDAO.getAccountByNumber(accountNumber);
            if (acc == null) {
                throw new AccountNotFoundException(accountNumber);
            }
            return acc;
        } catch (SQLException e) {
            throw new Exception("Database error fetching account: " + e.getMessage(), e);
        }
    }

    public synchronized void deposit(String accountNumber, double amount) throws Exception {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive.");
        }
        Account account = getAccount(accountNumber);
        account.deposit(amount); // Polymorphic deposit method
        try {
            accountDAO.updateBalance(accountNumber, account.getBalance());
            transactionDAO.recordTransaction(
                new Transaction(account.getId(), "DEPOSIT", amount, "Deposit to " + accountNumber)
            );
        } catch (SQLException e) {
            throw new Exception("Database error during deposit: " + e.getMessage(), e);
        }
    }

    public synchronized void withdraw(String accountNumber, double amount)
            throws AccountNotFoundException, InsufficientFundsException, InvalidAmountException, Exception {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive.");
        }
        Account account = getAccount(accountNumber);
        account.withdraw(amount); // Checks balance / overdraft limit
        try {
            accountDAO.updateBalance(accountNumber, account.getBalance());
            transactionDAO.recordTransaction(
                new Transaction(account.getId(), "WITHDRAW", amount, "Withdrawal from " + accountNumber)
            );
        } catch (SQLException e) {
            throw new Exception("Database error during withdrawal: " + e.getMessage(), e);
        }
    }

    // ─── Multithreaded Fund Transfer (Module 3) ─────────────────────────────

    public void transfer(String fromAccNumber, String toAccNumber, double amount) throws Exception {
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be positive.");
        }
        if (fromAccNumber.equalsIgnoreCase(toAccNumber)) {
            throw new InvalidAmountException("Cannot transfer to the same account.");
        }

        // Executes transfer on a dedicated worker thread (Module 3 Multithreading)
        TransferThread thread = new TransferThread(fromAccNumber, toAccNumber, amount, this);
        thread.start();
        thread.join(); // Wait for thread to finish execution

        if (thread.getException() != null) {
            Exception ex = thread.getException();
            if (ex instanceof AccountNotFoundException) throw (AccountNotFoundException) ex;
            if (ex instanceof InsufficientFundsException) throw (InsufficientFundsException) ex;
            if (ex instanceof InvalidAmountException) throw (InvalidAmountException) ex;
            throw ex;
        }
    }

    // Internal Atomic Transfer with JDBC Transaction (Module 4)
    public synchronized void executeTransfer(String fromAccNumber, String toAccNumber, double amount) throws Exception {
        Account from = getAccount(fromAccNumber);
        Account to = getAccount(toAccNumber);

        from.withdraw(amount);
        to.deposit(amount);

        // JDBC Transaction Management
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                accountDAO.updateBalance(fromAccNumber, from.getBalance(), conn);
                accountDAO.updateBalance(toAccNumber, to.getBalance(), conn);

                transactionDAO.recordTransaction(
                    new Transaction(from.getId(), "TRANSFER_OUT", amount, "Transfer to " + toAccNumber), conn
                );
                transactionDAO.recordTransaction(
                    new Transaction(to.getId(), "TRANSFER_IN", amount, "Transfer from " + fromAccNumber), conn
                );

                conn.commit(); // Commit transaction if all succeed
            } catch (SQLException e) {
                conn.rollback(); // Rollback if any error occurs
                throw e;
            }
        }
    }

    // ─── FD & Loan Mathematical Calculations (Module 1: Math class) ────────

    /**
     * Compound Interest on Fixed Deposit using Math.pow() (UCS202 Module 1)
     * Formula: A = P * (1 + r/4)^(4*t)
     */
    public double calculateFDMaturity(double principal, double annualRatePercent, int years) {
        if (principal <= 0 || annualRatePercent <= 0 || years <= 0) return 0.0;
        double r = (annualRatePercent / 100.0) / 4.0;
        double n = 4.0 * years;
        return principal * Math.pow(1.0 + r, n);
    }

    /**
     * Loan Monthly EMI Calculation using Math.pow() (UCS202 Module 1)
     * Formula: EMI = [P * r * (1+r)^n] / [(1+r)^n - 1]
     */
    public double calculateLoanEMI(double principal, double annualRatePercent, int years) {
        if (principal <= 0 || annualRatePercent <= 0 || years <= 0) return 0.0;
        double monthlyRate = (annualRatePercent / 100.0) / 12.0;
        int totalMonths = years * 12;
        double factor = Math.pow(1.0 + monthlyRate, totalMonths);
        return (principal * monthlyRate * factor) / (factor - 1.0);
    }

    public synchronized void bookFixedDeposit(String accountNumber, double amount, int years, double rate) throws Exception {
        Account account = getAccount(accountNumber);
        account.withdraw(amount);
        try {
            accountDAO.updateBalance(accountNumber, account.getBalance());
            double maturity = calculateFDMaturity(amount, rate, years);
            String description = String.format("FD Created: ₹%.0f for %d yrs @ %.1f%% (Maturity: ₹%.0f)",
                    amount, years, rate, maturity);
            transactionDAO.recordTransaction(
                new Transaction(account.getId(), "WITHDRAW", amount, description)
            );
        } catch (SQLException e) {
            throw new Exception("Database error booking Fixed Deposit: " + e.getMessage(), e);
        }
    }

    // ─── Profile & Security ────────────────────────────────────────────────

    public boolean updateProfile(int customerId, String name, String phone) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("Name cannot be empty.");
        }
        try {
            return customerDAO.updateProfile(customerId, name.trim(), phone != null ? phone.trim() : "");
        } catch (SQLException e) {
            throw new Exception("Database error updating profile: " + e.getMessage(), e);
        }
    }

    public boolean changePassword(int customerId, String currentPassword, String newPassword) throws Exception {
        if (newPassword == null || newPassword.length() < 4) {
            throw new Exception("New password must be at least 4 characters long.");
        }
        try {
            boolean ok = customerDAO.changePassword(customerId, currentPassword, newPassword);
            if (!ok) {
                throw new Exception("Current password is incorrect.");
            }
            return true;
        } catch (SQLException e) {
            throw new Exception("Database error changing password: " + e.getMessage(), e);
        }
    }

    // ─── Transaction History & File Export (Module 1: File I/O) ────────────

    public List<Transaction> getTransactionHistory(int customerId) {
        try {
            return transactionDAO.getAllTransactionsByCustomer(customerId);
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void exportStatement(int customerId, String customerName, List<Transaction> transactions, String filePath)
            throws IOException {
        try (FileWriter fw = new FileWriter(filePath)) {
            fw.write("=======================================================\n");
            fw.write("      HORIZON TRUST BANK - ACCOUNT STATEMENT\n");
            fw.write("=======================================================\n");
            fw.write("Customer Name: " + customerName + "\n");
            fw.write("Generated On : " + new java.util.Date() + "\n");
            fw.write("-------------------------------------------------------\n");
            fw.write(String.format("%-6s %-15s %-12s %-20s\n", "TXN ID", "Type", "Amount(₹)", "Date"));
            fw.write("-------------------------------------------------------\n");
            for (Transaction t : transactions) {
                fw.write(String.format("%-6d %-15s %-12.2f %-20s\n",
                    t.getId(), t.getType(), t.getAmount(), t.getTimestamp()));
            }
            fw.write("=======================================================\n");
        }
    }

    // ─── Account Number Generator ──────────────────────────────────────────

    private String generateAccountNumber() {
        Random rand = new Random();
        while (true) {
            long num = 1000000000L + (long)(rand.nextDouble() * 9000000000L);
            String accNumber = "ACC" + num;
            try {
                if (!accountDAO.accountNumberExists(accNumber)) {
                    return accNumber;
                }
            } catch (SQLException e) {
                return accNumber;
            }
        }
    }
}
