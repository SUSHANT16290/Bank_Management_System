package com.bank.util;

/**
 * ValidationUtil - Input validation and basic sanitization.
 */
public final class ValidationUtil {
    private ValidationUtil() {}

    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim();
    }

    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }

    public static void validatePositiveAmount(double amount, String fieldName) {
        if (amount <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }

    public static void validatePositiveInt(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }

    public static void validateNotEqual(String a, String b, String message) {
        if (a != null && a.equalsIgnoreCase(b)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void validateAccountNumber(String accountNumber) {
        validateNotEmpty(accountNumber, "Account number");
        if (accountNumber.trim().length() < 5) {
            throw new IllegalArgumentException("Invalid account number format.");
        }
    }

    public static void validateRegistration(String name, String email, String phone, String password) {
        validateNotEmpty(name, "Name");
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters.");
        }
        validateNotEmpty(email, "Email");
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        validateNotEmpty(password, "Password");
        if (password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters.");
        }
    }

    public static void validateProfileUpdate(String name, String phone) {
        validateNotEmpty(name, "Name");
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters.");
        }
    }

    public static void validatePasswordChange(String currentPassword, String newPassword) {
        validateNotEmpty(currentPassword, "Current password");
        validateNotEmpty(newPassword, "New password");
        if (newPassword.length() < 4) {
            throw new IllegalArgumentException("New password must be at least 4 characters.");
        }
    }
}
