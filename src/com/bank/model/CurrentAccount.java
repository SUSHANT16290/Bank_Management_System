package com.bank.model;

public class CurrentAccount extends Account {
    private static final double INTEREST_RATE = 0.0; // No interest on current accounts
    private static final double MINIMUM_BALANCE = 1000.0;
    private static final double OVERDRAFT_LIMIT = 5000.0;

    public CurrentAccount() {
        super();
        this.accountType = "CURRENT";
    }

    public CurrentAccount(String accountNumber, int customerId, double balance) {
        super(accountNumber, customerId, balance);
        this.accountType = "CURRENT";
    }

    @Override
    public String getAccountType() {
        return "CURRENT";
    }

    @Override
    public double calculateInterest() {
        return INTEREST_RATE; // No interest
    }

    @Override
    public double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }

    public double getOverdraftLimit() {
        return OVERDRAFT_LIMIT;
    }
}
