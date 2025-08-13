package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final EncryptionService encryptionService;

    // ADMIN: Создание карты
    @PostMapping
    public ResponseEntity<CardDTO> createCard(@RequestBody CardCreateRequest request) {
        CardDTO card = convertToDTO(cardService.createCard(
                request.getUserId(),
                request.getCardNumber(),
                request.getExpiryDate()
        ));
        return ResponseEntity.ok(card);
    }

    // USER: Получение своих карт (с пагинацией)
    @GetMapping
    public ResponseEntity<Page<CardDTO>> getUserCards(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Long userId = ((User) userDetails).getId();
        Page<CardDTO> cards = cardService.getUserCards(userId, pageable)
                .map(this::convertToDTO);
        return ResponseEntity.ok(cards);
    }

    // ADMIN/USER: Получение карты по ID (с проверкой прав)
    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getCardById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        CardDTO card = convertToDTO(cardService.getCardById(id));

        // Проверка принадлежности карты (для USER)
        if (user.getRole() == Role.USER &&
                !card.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(card);
    }

    // ADMIN: Блокировка карты
    @PutMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockCard(@PathVariable Long id) {
        cardService.blockCard(id);
        return ResponseEntity.ok().build();
    }

    // USER: Запрос на блокировку карты
    @PutMapping("/{id}/request-block")
    public ResponseEntity<Void> requestBlockCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;

        // Проверка принадлежности карты
        if (!cardService.isCardOwnedByUser(id, user.getId())) {
            return ResponseEntity.status(403).build();
        }

        // В реальном приложении здесь была бы отправка запроса админу
        cardService.blockCard(id);
        return ResponseEntity.ok().build();
    }

    // ADMIN: Активация карты
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateCard(@PathVariable Long id) {
        cardService.activateCard(id);
        return ResponseEntity.ok().build();
    }

    // ADMIN: Удаление карты
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    private CardDTO convertToDTO(BankCard card) {
        return new CardDTO(
                card.getId(),
                encryptionService.maskCardNumber(card.getEncryptedNumber()),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance(),
                card.getUser().getId()
        );
    }
}
