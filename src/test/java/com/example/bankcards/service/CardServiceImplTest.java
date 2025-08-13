package com.example.bankcards.service;

import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private BankCardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    void createCard_ValidData_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(encryptionService.encrypt("1234567812345678")).thenReturn("encrypted");
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        BankCard card = cardService.createCard(1L, "1234567812345678", "12/25");

        // Assert
        assertNotNull(card);
        assertEquals("encrypted", card.getEncryptedNumber());
        assertEquals(LocalDate.of(2025, 12, 31), card.getExpiryDate());
        assertEquals(BankCard.CardStatus.ACTIVE, card.getStatus());
        assertEquals(BigDecimal.ZERO, card.getBalance());
        assertEquals(user, card.getUser());
    }

    @Test
    void blockCard_ActiveCard_Success() {
        // Arrange
        BankCard card = new BankCard();
        card.setId(1L);
        card.setStatus(BankCard.CardStatus.ACTIVE);
        card.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // Act
        cardService.blockCard(1L);

        // Assert
        assertEquals(BankCard.CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_ExpiredCard_ThrowsException() {
        // Arrange
        BankCard card = new BankCard();
        card.setId(1L);
        card.setStatus(BankCard.CardStatus.EXPIRED);
        card.setExpiryDate(LocalDate.now().minusDays(1));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // Act & Assert
        assertThrows(CardOperationException.class, () -> cardService.blockCard(1L));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void activateCard_ValidCard_Success() {
        // Arrange
        BankCard card = new BankCard();
        card.setId(1L);
        card.setStatus(BankCard.CardStatus.BLOCKED);
        card.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // Act
        cardService.activateCard(1L);

        // Assert
        assertEquals(BankCard.CardStatus.ACTIVE, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void activateCard_ExpiredCard_ThrowsException() {
        // Arrange
        BankCard card = new BankCard();
        card.setId(1L);
        card.setStatus(BankCard.CardStatus.BLOCKED);
        card.setExpiryDate(LocalDate.now().minusDays(1));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // Act & Assert
        assertThrows(CardOperationException.class, () -> cardService.activateCard(1L));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void isCardOwnedByUser_ValidOwnership_ReturnsTrue() {
        // Arrange
        when(cardRepository.findByUserIdAndCardId(1L, 1L)).thenReturn(Optional.of(new BankCard()));

        // Act & Assert
        assertTrue(cardService.isCardOwnedByUser(1L, 1L));
    }

    @Test
    void isCardOwnedByUser_InvalidOwnership_ReturnsFalse() {
        // Arrange
        when(cardRepository.findByUserIdAndCardId(1L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertFalse(cardService.isCardOwnedByUser(1L, 1L));
    }
}
