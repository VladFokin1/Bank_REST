package com.example.bankcards.service.impl;

import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.TransferException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankCardRepository cardRepository;

    @Override
    @Transactional
    public Transaction transferBetweenOwnCards(Long senderCardId, Long receiverCardId, BigDecimal amount) {
        if (senderCardId.equals(receiverCardId)) {
            throw new TransferException("Cannot transfer to the same card");
        }

        BankCard senderCard = cardRepository.findById(senderCardId)
                .orElseThrow(() -> new IllegalArgumentException("Sender card not found"));

        BankCard receiverCard = cardRepository.findById(receiverCardId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver card not found"));

        // Проверка принадлежности карт одному пользователю
        if (!senderCard.getUser().getId().equals(receiverCard.getUser().getId())) {
            throw new TransferException("Cards must belong to the same user");
        }

        // Проверка статуса карт
        if (senderCard.getStatus() != BankCard.CardStatus.ACTIVE) {
            throw new TransferException("Sender card is not active");
        }

        if (receiverCard.getStatus() != BankCard.CardStatus.ACTIVE) {
            throw new TransferException("Receiver card is not active");
        }

        // Проверка баланса
        if (senderCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds on sender card");
        }

        // Выполнение перевода
        senderCard.setBalance(senderCard.getBalance().subtract(amount));
        receiverCard.setBalance(receiverCard.getBalance().add(amount));

        cardRepository.save(senderCard);
        cardRepository.save(receiverCard);

        // Создание записи о транзакции
        Transaction transaction = Transaction.builder()
                .amount(amount)
                .senderCard(senderCard)
                .receiverCard(receiverCard)
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getUserTransactions(Long userId) {
        // Получаем все карты пользователя
        List<BankCard> userCards = cardRepository.findByUserId(userId);

        // Преобразуем в Set для оптимизации запроса
        Set<BankCard> cardSet = new HashSet<>(userCards);

        // Используем новый метод репозитория
        return transactionRepository.findByUserCards(cardSet);
    }
}
