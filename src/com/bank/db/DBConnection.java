package com.bank.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Module 4: Java Database Connectivity (JDBC)
 * Handles database connection to MySQL using JDBC Driver.
 */
public class DBConnection {
    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/bank_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Password";

    // Load JDBC Driver in static block
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    private DBConnection() {}

    // Factory method to provide fresh database connections
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closeConnection() {
        // Safe shutdown hook handler
    }
}
