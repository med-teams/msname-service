package com.medteams.catalog.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank(message = "est obligatoire")
        @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9-]{2,31}", message = "3 a 32 caracteres : lettres, chiffres et tirets")
        String sku,

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
