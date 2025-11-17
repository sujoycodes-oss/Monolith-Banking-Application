package com.bankingsystem.exception;

public class InvalidAmountException extends CustomException {
    public InvalidAmountException() {
        super("Amount must be positive");
    }
}
