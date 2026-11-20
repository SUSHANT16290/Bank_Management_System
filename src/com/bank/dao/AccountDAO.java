package com.bank.dao;

import com.bank.db.DBConnection;
import com.bank.model.Account;
import com.bank.model.CurrentAccount;
import com.bank.model.SavingsAccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AccountDAO - Module 4: Database Connectivity (CRUD Operations)
 * Handles SQL operations on bank accounts (Savings & Current).
 */
public class AccountDAO {

    // Create a new account in database
    public boolean createAccount(Account account) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return createAccount(account, conn);
        }
    }

    public boolean createAccount(Account account, Connection conn) throws SQLException {
        String sql = "INSERT INTO accounts (account_number, customer_id, account_type, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account.getAccountNumber());
            ps.setInt(2, account.getCustomerId());
            ps.setString(3, account.getAccountType());
            ps.setDouble(4, account.getBalance());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        account.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // Retrieve an account by account number
    public Account getAccountByNumber(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildAccountFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Retrieve all accounts for a customer
    public List<Account> getAccountsByCustomer(int customerId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(buildAccountFromResultSet(rs));
                }
            }
        }
        return accounts;
    }

    // Update account balance (supports transaction connection)
    public boolean updateBalance(String accountNumber, double newBalance) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return updateBalance(accountNumber, newBalance, conn);
        }
    }

    public boolean updateBalance(String accountNumber, double newBalance, Connection conn) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accountNumber);
            return ps.executeUpdate() > 0;
        }
    }

    // Check if account number exists
    public boolean accountNumberExists(String accountNumber) throws SQLException {
        String sql = "SELECT id FROM accounts WHERE account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Helper: Map ResultSet to SavingsAccount or CurrentAccount (Polymorphism in Module 2)
    private Account buildAccountFromResultSet(ResultSet rs) throws SQLException {
        String type = rs.getString("account_type");
        int id = rs.getInt("id");
        String accNum = rs.getString("account_number");
        int custId = rs.getInt("customer_id");
        double bal = rs.getDouble("balance");

        Account acc;
        if ("CURRENT".equalsIgnoreCase(type)) {
            acc = new CurrentAccount(accNum, custId, bal);
        } else {
            acc = new SavingsAccount(accNum, custId, bal);
        }
        acc.setId(id);
        return acc;
    }
}
