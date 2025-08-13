package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends ApiException {
    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s with ID %d not found", entityName, id), HttpStatus.NOT_FOUND);
    }

    public EntityNotFoundException(String entityName, String identifier) {
        super(String.format("%s '%s' not found", entityName, identifier), HttpStatus.NOT_FOUND);
    }
}
