package com.bankingsystem.service;

import com.bankingsystem.exception.AccountNotFoundException;
import com.bankingsystem.exception.InsufficientBalanceException;
import com.bankingsystem.exception.InvalidAmountException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.dto.AccountRequest;
import com.bankingsystem.model.dto.ApiResponse;
import com.bankingsystem.model.dto.TransferRequest;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txnRepo;
    private static final int MAX_RETRIES = 3;
    private static final Random RANDOM = new Random();

    public Account createAccount(AccountRequest req) {
        String accNum;
        do {
            accNum = generateAccountNumber(req.getHolderName());
        } while (accountRepo.existsByAccountNumber(accNum));

        Account account = Account.builder()
                .accountNumber(accNum)
                .holderName(req.getHolderName())
                .balance(0L)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        return accountRepo.save(account);
    }

    public Account getAccount(String accNum) {
        return accountRepo.findByAccountNumber(accNum)
                .orElseThrow(() -> new AccountNotFoundException(accNum));
    }

    public ApiResponse deposit(String accNum, double amount) {
        if (amount <= 0) throw new InvalidAmountException();

        int tries = 0;
        while (true) {
            tries++;
            try {
                Account acc = getAccount(accNum);
                acc.setBalance(acc.getBalance() + amount);
                accountRepo.save(acc);

                Transaction txn = Transaction.builder()
                        .transactionId(generateTxnId())
                        .type("DEPOSIT")
                        .amount(amount)
                        .timestamp(Instant.now())
                        .status("SUCCESS")
                        .sourceAccount(accNum)
                        .build();

                txnRepo.save(txn);

                return new ApiResponse(true, "Deposit successful", txn);

            } catch (OptimisticLockingFailureException ex) {
                if (tries >= MAX_RETRIES) throw ex;
            }
        }
    }


    public ApiResponse withdraw(String accNum, double amount) {
        if (amount <= 0) throw new InvalidAmountException();

        int tries = 0;
        while (true) {
            tries++;
            try {
                Account acc = getAccount(accNum);
                if (acc.getBalance() < amount) throw new InsufficientBalanceException();

                acc.setBalance(acc.getBalance() - amount);
                accountRepo.save(acc);

                Transaction txn = Transaction.builder()
                        .transactionId(generateTxnId())
                        .type("WITHDRAW")
                        .amount(amount)
                        .timestamp(Instant.now())
                        .status("SUCCESS")
                        .sourceAccount(accNum)
                        .build();

                txnRepo.save(txn);

                return new ApiResponse(true, "Withdrawal successful", txn);

            } catch (OptimisticLockingFailureException ex) {
                if (tries >= MAX_RETRIES) throw ex;
            }
        }
    }


    public ApiResponse transfer(TransferRequest req) {
        if (req.getAmount() <= 0) throw new InvalidAmountException();
        if (req.getFromAccount().equals(req.getToAccount())) throw new IllegalArgumentException("Cannot transfer to same account");

        int tries = 0;
        while (true) {
            tries++;
            try {
                Account from = getAccount(req.getFromAccount());
                Account to = getAccount(req.getToAccount());

                if (from.getBalance() < req.getAmount()) throw new InsufficientBalanceException();

                from.setBalance(from.getBalance() - req.getAmount());
                to.setBalance(to.getBalance() + req.getAmount());

                accountRepo.save(from);
                accountRepo.save(to);

                Transaction txn = Transaction.builder()
                        .transactionId(generateTxnId())
                        .type("TRANSFER")
                        .amount(req.getAmount())
                        .timestamp(Instant.now())
                        .status("SUCCESS")
                        .sourceAccount(req.getFromAccount())
                        .destinationAccount(req.getToAccount())
                        .build();

                Transaction savedTxn = txnRepo.save(txn);

                return new ApiResponse(true, "Transfer successful", savedTxn);
            } catch (OptimisticLockingFailureException ex) {
                if (tries >= MAX_RETRIES) throw ex;
            }
        }
    }

    public List<Transaction> getTransactions(String accNum) {
        getAccount(accNum);
        return txnRepo.findBySourceAccountOrDestinationAccount(accNum, accNum);
    }

    private String generateTxnId() {
        return "TXN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" +
                String.format("%03d", RANDOM.nextInt(1000));
    }

    private String generateAccountNumber(String holderName) {
        String init = holderName.trim().toUpperCase().split(" ")[0];
        return init + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}