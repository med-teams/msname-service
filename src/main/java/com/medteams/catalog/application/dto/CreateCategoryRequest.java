package com.medteams.catalog.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(

        @NotBlank(message = "est obligatoire")
        @Size(max = 80, message = "80 caracteres maximum")
        String name,

        @Size(max = 500, message = "500 caracteres maximum")
        String description
) {
}
