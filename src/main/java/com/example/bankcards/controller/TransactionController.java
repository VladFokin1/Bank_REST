package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CardService cardService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO> transfer(
            @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;

        // Проверка принадлежности карт пользователю
        if (!cardService.isCardOwnedByUser(request.getFromCardId(), user.getId()) ||
                !cardService.isCardOwnedByUser(request.getToCardId(), user.getId())) {
            return ResponseEntity.status(403).build();
        }

        TransactionDTO transaction = convertToDTO(transactionService.transferBetweenOwnCards(
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount()
        ));

        return ResponseEntity.ok(transaction);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getUserTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        List<TransactionDTO> transactions = transactionService.getUserTransactions(user.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getTimestamp(),
                transaction.getSenderCard().getId(),
                transaction.getReceiverCard().getId()
        );
    }
}