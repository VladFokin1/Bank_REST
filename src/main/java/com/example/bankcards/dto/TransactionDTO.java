package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private Long senderCardId;
    private Long receiverCardId;
}