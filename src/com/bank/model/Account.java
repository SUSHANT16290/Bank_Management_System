package com.bank.model;

import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.interfaces.Transactable;

public abstract class Account implements Transactable {
    protected int id;
    protected String accountNumber;
    protected int customerId;
    protected double balance;
    protected String accountType;

    public Account() {}

    public Account(String accountNumber, int customerId, double balance) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.balance = balance;
    }

    // Abstract methods — must be overridden by subclasses
    public abstract String getAccountType();
    public abstract double calculateInterest();
    public abstract double getMinimumBalance();

    @Override
    public synchronized void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero.");
        }
        this.balance += amount;
    }

    @Override
    public synchronized void withdraw(double amount) throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }
        if (amount > this.balance) {
            throw new InsufficientFundsException(amount);
        }
        this.balance -= amount;
    }

    @Override
    public double getBalance() { return balance; }

    @Override
    public String getAccountNumber() { return accountNumber; }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setBalance(double balance) { this.balance = balance; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    @Override
    public String toString() {
        return getAccountType() + " Account [" + accountNumber + "] Balance: ₹" + String.format("%.2f", balance);
    }
}
