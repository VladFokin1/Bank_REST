package com.example.bankcards.service;

import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.TransferException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankCardRepository cardRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void transferBetweenOwnCards_ValidTransfer_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);

        BankCard senderCard = new BankCard();
        senderCard.setId(1L);
        senderCard.setBalance(BigDecimal.valueOf(1000));
        senderCard.setStatus(BankCard.CardStatus.ACTIVE);
        senderCard.setUser(user);

        BankCard receiverCard = new BankCard();
        receiverCard.setId(2L);
        receiverCard.setBalance(BigDecimal.valueOf(500));
        receiverCard.setStatus(BankCard.CardStatus.ACTIVE);
        receiverCard.setUser(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Transaction transaction = transactionService.transferBetweenOwnCards(1L, 2L, BigDecimal.valueOf(300));

        // Assert
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(300), transaction.getAmount());
        assertEquals(senderCard, transaction.getSenderCard());
        assertEquals(receiverCard, transaction.getReceiverCard());

        assertEquals(BigDecimal.valueOf(700), senderCard.getBalance());
        assertEquals(BigDecimal.valueOf(800), receiverCard.getBalance());

        verify(cardRepository).save(senderCard);
        verify(cardRepository).save(receiverCard);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void transferBetweenOwnCards_SameCard_ThrowsException() {
        // Act & Assert
        assertThrows(TransferException.class, () ->
                transactionService.transferBetweenOwnCards(1L, 1L, BigDecimal.valueOf(100)));
    }

    @Test
    void transferBetweenOwnCards_DifferentUsers_ThrowsException() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        BankCard senderCard = new BankCard();
        senderCard.setId(1L);
        senderCard.setUser(user1);

        BankCard receiverCard = new BankCard();
        receiverCard.setId(2L);
        receiverCard.setUser(user2);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));

        // Act & Assert
        assertThrows(TransferException.class, () ->
                transactionService.transferBetweenOwnCards(1L, 2L, BigDecimal.valueOf(100)));
    }

    @Test
    void transferBetweenOwnCards_InactiveSender_ThrowsException() {
        // Arrange
        User user = new User();
        user.setId(1L);

        BankCard senderCard = new BankCard();
        senderCard.setId(1L);
        senderCard.setStatus(BankCard.CardStatus.BLOCKED);
        senderCard.setUser(user);

        BankCard receiverCard = new BankCard();
        receiverCard.setId(2L);
        receiverCard.setStatus(BankCard.CardStatus.ACTIVE);
        receiverCard.setUser(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));

        // Act & Assert
        assertThrows(TransferException.class, () ->
                transactionService.transferBetweenOwnCards(1L, 2L, BigDecimal.valueOf(100)));
    }

    @Test
    void transferBetweenOwnCards_InsufficientFunds_ThrowsException() {
        // Arrange
        User user = new User();
        user.setId(1L);

        BankCard senderCard = new BankCard();
        senderCard.setId(1L);
        senderCard.setBalance(BigDecimal.valueOf(100));
        senderCard.setStatus(BankCard.CardStatus.ACTIVE);
        senderCard.setUser(user);

        BankCard receiverCard = new BankCard();
        receiverCard.setId(2L);
        receiverCard.setStatus(BankCard.CardStatus.ACTIVE);
        receiverCard.setUser(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () ->
                transactionService.transferBetweenOwnCards(1L, 2L, BigDecimal.valueOf(200)));
    }
}
