package com.bankingsystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    private String id;

    @Indexed(unique = true)
    private String transactionId;

    private String type;

    private double amount;

    private Instant timestamp;

    private String status;

    private String sourceAccount;
    private String destinationAccount;
}