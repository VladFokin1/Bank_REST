package com.example.bankcards.service;

import com.example.bankcards.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    Transaction transferBetweenOwnCards(Long senderCardId, Long receiverCardId, BigDecimal amount);
    List<Transaction> getUserTransactions(Long userId);
}
