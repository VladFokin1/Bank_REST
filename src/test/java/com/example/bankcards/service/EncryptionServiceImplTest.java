package com.example.bankcards.service;

import com.example.bankcards.service.impl.EncryptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EncryptionServiceImplTest {

    private EncryptionServiceImpl encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionServiceImpl();
        ReflectionTestUtils.setField(encryptionService, "secretKey", "32-char-secret-key-1234567890");
    }

    @Test
    void encryptDecrypt_ValidData_ReturnsOriginal() {
        // Arrange
        String original = "1234567812345678";

        // Act
        String encrypted = encryptionService.encrypt(original);
        String decrypted = encryptionService.decrypt(encrypted);

        // Assert
        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void maskCardNumber_ValidCard_ReturnsMasked() {
        // Arrange
        String cardNumber = "1234567812345678";
        String encrypted = encryptionService.encrypt(cardNumber);

        // Act
        String masked = encryptionService.maskCardNumber(encrypted);

        // Assert
        assertEquals("**** **** **** 5678", masked);
    }

    @Test
    void encrypt_EmptyString_ThrowsException() {
        assertThrows(RuntimeException.class, () -> encryptionService.encrypt(""));
    }

    @Test
    void decrypt_InvalidData_ThrowsException() {
        assertThrows(RuntimeException.class, () -> encryptionService.decrypt("invalid-encrypted-data"));
    }
}
