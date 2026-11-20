package com.bank.interfaces;

public interface Transactable {
    void deposit(double amount) throws Exception;
    void withdraw(double amount) throws Exception;
    double getBalance();
    String getAccountNumber();
}
