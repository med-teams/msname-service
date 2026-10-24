package com.example.msname.infrastructure.exception;

import com.example.commons.api.dto.ErrorResponse;
import com.example.commons.api.exception.ConflictException;
import com.example.commons.api.exception.ErrorCodes;
import com.example.commons.api.exception.ForbiddenException;
import com.example.commons.api.exception.ResourceNotFoundException;
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
        assertEquals(ErrorCodes.CONFLICT, response.getBody().getCode());
        assertEquals("Produit deja existant", response.getBody().getMessage());
    }

    @Test
    void shouldMapNotFoundTo404() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Produit introuvable");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCodes.NOT_FOUND, response.getBody().getCode());
        assertEquals("Produit introuvable", response.getBody().getMessage());
    }

    @Test
    void shouldMapForbiddenTo403() {
        // Given
        ForbiddenException exception = new ForbiddenException("Acces refuse");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleForbidden(exception);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCodes.FORBIDDEN, response.getBody().getCode());
        assertEquals("Acces refuse", response.getBody().getMessage());
    }
}
