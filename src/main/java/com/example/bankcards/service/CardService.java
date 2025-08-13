package com.example.bankcards.service;

import com.example.bankcards.entity.BankCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CardService {
    BankCard createCard(Long userId, String cardNumber, String expiryDate);
    void blockCard(Long cardId);
    void activateCard(Long cardId);
    void deleteCard(Long cardId);
    Page<BankCard> getUserCards(Long userId, Pageable pageable);
    List<BankCard> getUserCardsForTransfer(Long userId);
    BankCard getCardById(Long cardId);
    BigDecimal getCardBalance(Long cardId);
    boolean isCardOwnedByUser(Long cardId, Long userId);
}
