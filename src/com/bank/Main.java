package com.bank;

import com.bank.db.DBConnection;
import com.bank.ui.LoginFrame;

import javax.swing.*;

/**
 * Main entry point for Bank Management System.
 * Covers: UCS202 — OOP using Java
 * Modules: Java Basics | Inheritance | Multithreading | JDBC | Swing GUI
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}

            Runtime.getRuntime().addShutdownHook(new Thread(DBConnection::closeConnection));

            new LoginFrame().setVisible(true);
        });
    }
}