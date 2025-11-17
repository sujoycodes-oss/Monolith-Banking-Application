package com.bankingsystem.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransferRequest {
    @NotBlank
    private String fromAccount;

    @NotBlank
    private String toAccount;

    @Positive
    private double amount;
}
