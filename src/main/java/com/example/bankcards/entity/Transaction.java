package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_card_id", nullable = false)
    private BankCard senderCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_card_id", nullable = false)
    private BankCard receiverCard;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
