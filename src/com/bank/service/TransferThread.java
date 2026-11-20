package com.bank.service;

/**
 * TransferThread - Module 3: Multithreading in Java
 * Demonstrates thread lifecycle and concurrent processing of money transfers.
 */
public class TransferThread extends Thread {
    private final String fromAccount;
    private final String toAccount;
    private final double amount;
    private final BankService bankService;
    private Exception exception;

    public TransferThread(String fromAccount, String toAccount, double amount, BankService bankService) {
        super("TransferWorker-" + fromAccount + "->" + toAccount);
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.bankService = bankService;
    }

    @Override
    public void run() {
        try {
            System.out.println("[" + getName() + "] Processing transfer of ₹" + amount 
                    + " from " + fromAccount + " to " + toAccount);
            bankService.executeTransfer(fromAccount, toAccount, amount);
            System.out.println("[" + getName() + "] Transfer completed successfully.");
        } catch (Exception e) {
            this.exception = e;
            System.err.println("[" + getName() + "] Transfer error: " + e.getMessage());
        }
    }

    public Exception getException() {
        return exception;
    }
}
