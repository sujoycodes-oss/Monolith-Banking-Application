package com.bankingsystem.controller;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.dto.AccountRequest;
import com.bankingsystem.model.dto.ApiResponse;
import com.bankingsystem.model.dto.TransferRequest;
import com.bankingsystem.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AccountController {

    private final AccountService service;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody AccountRequest req) {
        Account acc = service.createAccount(req);
        return ResponseEntity.status(201).body(new ApiResponse(true, "Account created", acc));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse> get(@PathVariable String accountNumber) {
        Account acc = service.getAccount(accountNumber);
        return ResponseEntity.ok(new ApiResponse(true, "Success", acc));
    }

    @PutMapping("/{accountNumber}/deposit")
    public ResponseEntity<ApiResponse> deposit(@PathVariable String accountNumber, @RequestParam double amount) {
        return ResponseEntity.ok(service.deposit(accountNumber, amount));
    }

    @PutMapping("/{accountNumber}/withdraw")
    public ResponseEntity<ApiResponse> withdraw(@PathVariable String accountNumber, @RequestParam double amount) {
        return ResponseEntity.ok(service.withdraw(accountNumber, amount));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse> transfer(@Valid @RequestBody TransferRequest req) {
        ApiResponse res = service.transfer(req);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<ApiResponse> transactions(@PathVariable String accountNumber) {
        List<Transaction> txns = service.getTransactions(accountNumber);
        return ResponseEntity.ok(new ApiResponse(true, "Transactions fetched", txns));
    }
}