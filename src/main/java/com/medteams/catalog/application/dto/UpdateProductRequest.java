package com.medteams.catalog.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Modification d'un produit : le SKU et le statut ne se modifient pas ici.
 */
public record UpdateProductRequest(

        @NotBlank(message = "est obligatoire")
        @Size(max = 120, message = "120 caracteres maximum")
        String name,

        @Size(max = 1000, message = "1000 caracteres maximum")
        String description,

        @NotNull(message = "est obligatoire")
        @Positive(message = "doit etre superieur a zero")
        @Digits(integer = 10, fraction = 2, message = "10 chiffres et 2 decimales maximum")
        BigDecimal price,

        @NotNull(message = "est obligatoire")
        Long categoryId
) {
}
