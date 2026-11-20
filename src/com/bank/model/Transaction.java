package com.bank.model;

import java.sql.Timestamp;

public class Transaction {
    private int id;
    private int accountId;
    private String type; // DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT
    private double amount;
    private String description;
    private Timestamp timestamp;

    public Transaction() {}

    public Transaction(int accountId, String type, double amount, String description) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "Transaction{type=" + type + ", amount=₹" + amount + ", desc=" + description + ", time=" + timestamp + "}";
    }
}
