package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private CardService cardService; // Нужен для проверки принадлежности карт

    // Вспомогательный метод для создания тестовой Transaction
    private Transaction createTestTransaction() {
        User user = new User();
        user.setId(1L);

        BankCard senderCard = new BankCard();
        senderCard.setId(1L);
        senderCard.setUser(user);

        BankCard receiverCard = new BankCard();
        receiverCard.setId(2L);
        receiverCard.setUser(user);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setSenderCard(senderCard);
        transaction.setReceiverCard(receiverCard);

        return transaction;
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void transferBetweenOwnCards_ValidRequest_Success() throws Exception {
        // 1. Создаем тестовую сущность Transaction
        Transaction transaction = createTestTransaction();

        // 2. Мокаем возврат Transaction
        when(transactionService.transferBetweenOwnCards(anyLong(), anyLong(), any()))
                .thenReturn(transaction);

        // 3. Мокаем проверку принадлежности карт
        when(cardService.isCardOwnedByUser(1L, 1L)).thenReturn(true);
        when(cardService.isCardOwnedByUser(2L, 1L)).thenReturn(true);

        // Подготовка запроса
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));

        // Выполнение и проверка
        mockMvc.perform(post("/api/transactions/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.senderCardId").value(1))
                .andExpect(jsonPath("$.receiverCardId").value(2));
    }


    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void transferBetweenOwnCards_NotOwnCard_Forbidden() throws Exception {
        // Подготовка
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(3L); // Чужая карта
        request.setAmount(BigDecimal.valueOf(100));

        when(cardService.isCardOwnedByUser(1L, 1L)).thenReturn(true);
        when(cardService.isCardOwnedByUser(3L, 1L)).thenReturn(false);

        // Выполнение и проверка
        mockMvc.perform(post("/api/transactions/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
