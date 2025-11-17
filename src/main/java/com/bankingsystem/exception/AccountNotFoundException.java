package com.bankingsystem.exception;

public class AccountNotFoundException extends CustomException {
    public AccountNotFoundException(String acc) {
        super("Account not found: " + acc);
    }
}