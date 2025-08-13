package com.example.bankcards.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CardCreateRequest {
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String cardNumber;

    @Pattern(regexp = "\\d{2}/\\d{2}", message = "Expiry date must be in MM/yy format")
    private String expiryDate;

    private Long userId;
}
