package com.bank.exception;

public class InsufficientFundsException extends Exception {
    private double amount;

    public InsufficientFundsException(double amount) {
        super("Insufficient funds! You tried to withdraw: ₹" + amount);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
