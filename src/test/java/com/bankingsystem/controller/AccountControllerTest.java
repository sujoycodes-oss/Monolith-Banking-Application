package com.bankingsystem.controller;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.dto.AccountRequest;
import com.bankingsystem.model.dto.ApiResponse;
import com.bankingsystem.model.dto.TransferRequest;
import com.bankingsystem.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AccountService service;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void createEndpoint_returnsCreated() throws Exception {
        AccountRequest req = new AccountRequest();
        req.setHolderName("Alice");

        Account acc = Account.builder()
                .accountNumber("ALICE-1")
                .holderName("Alice")
                .balance(0L)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        when(service.createAccount(any(AccountRequest.class))).thenReturn(acc);

        mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accountNumber", is("ALICE-1")));
    }

    @Test
    void getEndpoint_returnsAccount() throws Exception {
        Account acc = Account.builder()
                .accountNumber("ALICE-1")
                .holderName("Alice")
                .balance(100L)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        when(service.getAccount("ALICE-1")).thenReturn(acc);

        mvc.perform(get("/api/accounts/ALICE-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance", is(100)));
    }

    @Test
    void depositEndpoint_returnsOk() throws Exception {
        Account acc = Account.builder()
                .accountNumber("ALICE-1")
                .holderName("Alice")
                .balance(500L)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        when(service.deposit("ALICE-1", 500L)).thenReturn(acc);

        mvc.perform(put("/api/accounts/ALICE-1/deposit")
                        .param("amount", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance", is(500)));
    }

    @Test
    void transferEndpoint_returnsOk() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("ALICE-1");
        req.setToAccount("ANJALI-1");
        req.setAmount(150L);

        when(service.transfer(any(TransferRequest.class))).thenReturn(new ApiResponse(true, "Transfer successful", null));

        mvc.perform(post("/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void transactionsEndpoint_returnsList() throws Exception {
        when(service.getTransactions("ALICE-1")).thenReturn(List.of(new Transaction()));
        mvc.perform(get("/api/accounts/ALICE-1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }
}
