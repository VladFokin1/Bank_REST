package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class CardOperationException extends ApiException {
    public CardOperationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
