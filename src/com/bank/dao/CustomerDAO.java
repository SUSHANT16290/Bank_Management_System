package com.bank.dao;

import com.bank.db.DBConnection;
import com.bank.model.Customer;

import java.sql.*;

/**
 * CustomerDAO - Module 4: Database Connectivity (CRUD Operations)
 * Handles database operations for customer accounts (Registration, Login, Profile).
 */
public class CustomerDAO {

    // Register a new customer in the database
    public boolean registerCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, email, phone, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getPassword());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        customer.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // Authenticate a customer during login
    public Customer loginCustomer(String email, String password) throws SQLException {
        String sql = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    // Compare password (supports direct match or hashed check)
                    if (matchesPassword(password, storedPassword)) {
                        return new Customer(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            storedPassword
                        );
                    }
                }
            }
        }
        return null;
    }

    // Check if an email is already registered
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM customers WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Update customer personal profile
    public boolean updateProfile(int customerId, String name, String phone) throws SQLException {
        String sql = "UPDATE customers SET name = ?, phone = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setInt(3, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    // Update customer password
    public boolean changePassword(int customerId, String currentPassword, String newPassword) throws SQLException {
        // Verify current password first
        String verifySql = "SELECT password FROM customers WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psVerify = conn.prepareStatement(verifySql)) {

            psVerify.setInt(1, customerId);
            try (ResultSet rs = psVerify.executeQuery()) {
                if (!rs.next() || !matchesPassword(currentPassword, rs.getString("password"))) {
                    return false;
                }
            }
        }

        // Update to new password
        String updateSql = "UPDATE customers SET password = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {

            psUpdate.setString(1, newPassword);
            psUpdate.setInt(2, customerId);
            return psUpdate.executeUpdate() > 0;
        }
    }

    // Helper: compares plain password or handles legacy formatted entries
    private boolean matchesPassword(String inputPassword, String storedPassword) {
        if (inputPassword == null || storedPassword == null) return false;
        if (inputPassword.equals(storedPassword)) return true;
        // If stored in legacy salt format (iterations:salt:hash), check end
        if (storedPassword.contains(":")) {
            String[] parts = storedPassword.split(":");
            if (parts.length == 3 && inputPassword.equals(parts[2])) return true;
        }
        return false;
    }
}
