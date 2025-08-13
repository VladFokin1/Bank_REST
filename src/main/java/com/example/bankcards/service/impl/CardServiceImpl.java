package com.example.bankcards.service.impl;

import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final BankCardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    @Override
    @Transactional
    public BankCard createCard(Long userId, String cardNumber, String expiryDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Проверка формата даты (MM/yy)
        if (!expiryDate.matches("\\d{2}/\\d{2}")) {
            throw new IllegalArgumentException("Invalid expiry date format. Use MM/yy");
        }

        // Преобразование в LocalDate
        String[] parts = expiryDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = 2000 + Integer.parseInt(parts[1]); // Преобразование YY в YYYY
        LocalDate expiry = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);

        BankCard card = BankCard.builder()
                .encryptedNumber(encryptionService.encrypt(cardNumber))
                .expiryDate(expiry)
                .status(BankCard.CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();

        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public void blockCard(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (card.getStatus() == BankCard.CardStatus.EXPIRED) {
            throw new CardOperationException("Cannot block an expired card");
        }

        card.setStatus(BankCard.CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void activateCard(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new CardOperationException("Cannot activate an expired card");
        }

        card.setStatus(BankCard.CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
        cardRepository.delete(card);
    }

    @Override
    public Page<BankCard> getUserCards(Long userId, Pageable pageable) {
        return cardRepository.findByUserId(userId, pageable);
    }

    @Override
    public List<BankCard> getUserCardsForTransfer(Long userId) {
        return cardRepository.findActiveCardsByUser(
                userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"))
        );
    }

    @Override
    public BankCard getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
    }

    @Override
    public BigDecimal getCardBalance(Long cardId) {
        return cardRepository.findById(cardId)
                .map(BankCard::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
    }

    @Override
    public boolean isCardOwnedByUser(Long cardId, Long userId) {
        return cardRepository.findByUserIdAndCardId(userId, cardId).isPresent();
    }
}
