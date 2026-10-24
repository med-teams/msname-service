package com.medteams.catalog.application.dto;

import com.medteams.catalog.domain.model.Product;
import com.medteams.catalog.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(Long id, String sku, String name, String description, BigDecimal price,
                              ProductStatus status, Long categoryId, Instant createdAt, Instant updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getSku().value(), product.getName(),
                product.getDescription(), product.getPrice(), product.getStatus(), product.getCategoryId(),
                product.getCreatedAt(), product.getUpdatedAt());
    }
}
