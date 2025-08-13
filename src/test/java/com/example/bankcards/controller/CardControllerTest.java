package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.EncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private EncryptionService encryptionService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_AdminAccess_Success() throws Exception {
        // Подготовка
        CardDTO cardDTO = new CardDTO(1L, "**** **** **** 1234",
                LocalDate.now().plusYears(2), BankCard.CardStatus.ACTIVE,
                BigDecimal.ZERO, 1L);

        when(cardService.createCard(anyLong(), anyString(), anyString()))
                .thenReturn(new BankCard());
        when(encryptionService.maskCardNumber(anyString())).thenReturn("**** **** **** 1234");

        // Выполнение и проверка
        mockMvc.perform(post("/api/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\":\"1234567812345678\",\"expiryDate\":\"12/25\",\"userId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 1234"));
    }

    // Вспомогательный метод для создания тестовой BankCard
    private BankCard createTestBankCard() {
        User user = new User();
        user.setId(1L);

        BankCard card = new BankCard();
        card.setId(1L);
        card.setEncryptedNumber("encrypted-1234567812345678");
        card.setExpiryDate(LocalDate.now().plusYears(2));
        card.setStatus(BankCard.CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setUser(user);

        return card;
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getUserCards_UserAccess_ReturnsCards() throws Exception {
        // 1. Создаем тестовую сущность BankCard
        BankCard bankCard = createTestBankCard();

        // 2. Создаем Page<BankCard>
        Page<BankCard> page = new PageImpl<>(Collections.singletonList(bankCard));

        // 3. Мокаем возврат Page<BankCard>
        when(cardService.getUserCards(anyLong(), any()))
                .thenReturn(page);

        // 4. Мокаем преобразование в маскированный номер
        when(encryptionService.maskCardNumber(anyString()))
                .thenReturn("**** **** **** 5678");

        // Выполнение и проверка
        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 5678"))
                .andExpect(jsonPath("$.content[0].balance").value(1000));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void requestBlockCard_OwnCard_Success() throws Exception {
        // Подготовка
        when(cardService.isCardOwnedByUser(1L, 1L)).thenReturn(true);

        // Выполнение и проверка
        mockMvc.perform(put("/api/cards/1/request-block")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user2", roles = "USER")
    void requestBlockCard_NotOwnCard_Forbidden() throws Exception {
        // Подготовка
        when(cardService.isCardOwnedByUser(1L, 2L)).thenReturn(false);

        // Выполнение и проверка
        mockMvc.perform(put("/api/cards/1/request-block")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
