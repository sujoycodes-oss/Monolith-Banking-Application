package com.bankingsystem.service;

import com.bankingsystem.exception.AccountNotFoundException;
import com.bankingsystem.exception.InsufficientBalanceException;
import com.bankingsystem.exception.InvalidAmountException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.dto.AccountRequest;
import com.bankingsystem.model.dto.TransferRequest;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private TransactionRepository txnRepo;

    @InjectMocks
    private AccountService service;

    private Account alice;
    private Account anjali;

    @BeforeEach
    void setUp() {
        alice = Account.builder()
                .id("1")
                .accountNumber("ALICE-1")
                .holderName("Alice Sharma")
                .balance(0L)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .version(0L)
                .build();

        anjali = Account.builder()
                .id("2")
                .accountNumber("ANJALI-1")
                .holderName("Anjali Verma")
                .balance(0L)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .version(0L)
                .build();
    }

    @Test
    void createAccount_success() {
        AccountRequest req = new AccountRequest();
        req.setHolderName("New User");
        when(accountRepo.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);

        Account created = service.createAccount(req);

        assertNotNull(created.getAccountNumber());
        assertEquals("New User", created.getHolderName());
        assertEquals(0L, created.getBalance());
        verify(accountRepo, times(1)).save(any(Account.class));
    }

    @Test
    void getAccount_found() {
        when(accountRepo.findByAccountNumber("ALICE-1")).thenReturn(Optional.of(alice));
        Account found = service.getAccount("ALICE-1");
        assertEquals("Alice Sharma", found.getHolderName());
    }

    @Test
    void getAccount_notFound() {
        when(accountRepo.findByAccountNumber("NOPE")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> service.getAccount("NOPE"));
    }

    @Test
    void deposit_success() {
        when(accountRepo.findByAccountNumber("ALICE-1")).thenReturn(Optional.of(alice));
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);
        when(txnRepo.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Account result = service.deposit("ALICE-1", 500L);
        assertEquals(500L, result.getBalance());
        verify(txnRepo, times(1)).save(any(Transaction.class));
    }

    @Test
    void deposit_invalidAmount() {
        assertThrows(InvalidAmountException.class, () -> service.deposit("ALICE-1", 0L));
        assertThrows(InvalidAmountException.class, () -> service.deposit("ALICE-1", -10L));
    }

    @Test
    void withdraw_success() {
        alice.setBalance(1000L);
        when(accountRepo.findByAccountNumber("ALICE-1")).thenReturn(Optional.of(alice));
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);
        when(txnRepo.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Account result = service.withdraw("ALICE-1", 400L);
        assertEquals(600L, result.getBalance());
        verify(txnRepo, times(1)).save(any(Transaction.class));
    }

    @Test
    void withdraw_insufficient() {
        alice.setBalance(100L);
        when(accountRepo.findByAccountNumber("ALICE-1")).thenReturn(Optional.of(alice));
        assertThrows(InsufficientBalanceException.class, () -> service.withdraw("ALICE-1", 200L));
    }

    @Test
    void transfer_success() {
        alice.setBalance(1000L);
        anjali.setBalance(200L);
        when(accountRepo.findByAccountNumber("ALICE-1")).thenReturn(Optional.of(alice));
        when(accountRepo.findByAccountNumber("ANJALI-1")).thenReturn(Optional.of(anjali));
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);
        when(txnRepo.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        TransferRequest req = new TransferRequest();
        req.setFromAccount("ALICE-1");
        req.setToAccount("ANJALI-1");
        req.setAmount(300L);

        var res = service.transfer(req);
        assertTrue(res.isSuccess());
        assertEquals(700L, alice.getBalance());
        assertEquals(500L, anjali.getBalance());
        verify(txnRepo, times(1)).save(any(Transaction.class));
    }

    @Test
    void transfer_insufficient() {
        alice.setBalance(100L);
        when(accountRepo.findByAccountNumber("ALICE-1")).thenReturn(Optional.of(alice));
        when(accountRepo.findByAccountNumber("ANJALI-1")).thenReturn(Optional.of(anjali));

        TransferRequest req = new TransferRequest();
        req.setFromAccount("ALICE-1");
        req.setToAccount("ANJALI-1");
        req.setAmount(200L);

        assertThrows(InsufficientBalanceException.class, () -> service.transfer(req));
    }

    @Test
    void getTransactions_returnsList() {
        when(accountRepo.findByAccountNumber("ALICE-1")).thenReturn(Optional.of(alice));
        when(txnRepo.findBySourceAccountOrDestinationAccount("ALICE-1", "ALICE-1")).thenReturn(List.of());
        List<?> list = service.getTransactions("ALICE-1");
        assertNotNull(list);
        verify(txnRepo, times(1)).findBySourceAccountOrDestinationAccount("ALICE-1","ALICE-1");
    }
}
