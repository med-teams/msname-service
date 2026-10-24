package com.medteams.catalog.domain.model;

/**
 * Cycle de vie d'un produit : DRAFT -> ACTIVE -> ARCHIVED. Un brouillon peut aussi etre archive
 * directement ; un produit archive est definitif.
 */
public enum ProductStatus {

    DRAFT,
    ACTIVE,
    ARCHIVED;

    public boolean canTransitionTo(ProductStatus target) {
        return switch (this) {
            case DRAFT -> target == ACTIVE || target == ARCHIVED;
            case ACTIVE -> target == ARCHIVED;
            case ARCHIVED -> false;
        };
    }
}
