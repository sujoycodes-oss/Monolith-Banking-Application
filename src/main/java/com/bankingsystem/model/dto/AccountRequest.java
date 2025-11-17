package com.bankingsystem.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountRequest {
    @NotBlank(message = "Holder name is required")
    private String holderName;
}
