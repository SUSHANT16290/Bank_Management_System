package com.bank.dao;

import com.bank.db.DBConnection;
import com.bank.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionDAO - Module 4: Database Connectivity (CRUD Operations)
 * Records and retrieves banking transaction history.
 */
public class TransactionDAO {

    // Record a transaction
    public boolean recordTransaction(Transaction transaction) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return recordTransaction(transaction, conn);
        }
    }

    // Record a transaction within a shared transaction connection
    public boolean recordTransaction(Transaction transaction, Connection conn) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, type, amount, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transaction.getAccountId());
            ps.setString(2, transaction.getType());
            ps.setDouble(3, transaction.getAmount());
            ps.setString(4, transaction.getDescription());
            return ps.executeUpdate() > 0;
        }
    }

    // Retrieve all transactions for a customer across their accounts
    public List<Transaction> getAllTransactionsByCustomer(int customerId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.* FROM transactions t "
                   + "JOIN accounts a ON t.account_id = a.id "
                   + "WHERE a.customer_id = ? "
                   + "ORDER BY t.timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction(
                        rs.getInt("account_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                    );
                    t.setId(rs.getInt("id"));
                    t.setTimestamp(rs.getTimestamp("timestamp"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    // Retrieve transactions for a specific account ID
    public List<Transaction> getTransactionsByAccount(int accountId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction(
                        rs.getInt("account_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                    );
                    t.setId(rs.getInt("id"));
                    t.setTimestamp(rs.getTimestamp("timestamp"));
                    list.add(t);
                }
            }
        }
        return list;
    }
}
