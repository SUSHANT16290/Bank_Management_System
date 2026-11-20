package com.bank.model;

public class SavingsAccount extends Account {
    private static final double INTEREST_RATE = 0.04; // 4% per annum
    private static final double MINIMUM_BALANCE = 500.0;

    public SavingsAccount() {
        super();
        this.accountType = "SAVINGS";
    }

    public SavingsAccount(String accountNumber, int customerId, double balance) {
        super(accountNumber, customerId, balance);
        this.accountType = "SAVINGS";
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    @Override
    public double calculateInterest() {
        return balance * INTEREST_RATE;
    }

    @Override
    public double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }
}
