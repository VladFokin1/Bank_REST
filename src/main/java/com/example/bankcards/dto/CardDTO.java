package com.example.bankcards.dto;
import com.example.bankcards.entity.BankCard;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CardDTO {
    private Long id;
    private String maskedNumber;
    private LocalDate expiryDate;
    private BankCard.CardStatus status;
    private BigDecimal balance;
    private Long userId;
}
