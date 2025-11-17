package com.bankingsystem.exception;

public class InsufficientBalanceException extends CustomException {
    public InsufficientBalanceException() {
        super("Insufficient balance");
    }
}
