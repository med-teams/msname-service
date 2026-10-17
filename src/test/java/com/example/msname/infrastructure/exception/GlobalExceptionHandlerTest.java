package com.example.msname.infrastructure.exception;

import com.example.commons.api.dto.ErrorResponse;
import com.example.commons.api.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldMapConflictTo409() {
        // Given
        ConflictException exception = new ConflictException("Produit deja existant");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleConflict(exception);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CONFLICT", response.getBody().getCode());
        assertEquals("Produit deja existant", response.getBody().getMessage());
    }
}
