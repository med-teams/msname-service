package com.medteams.catalog.domain.repository;

import com.medteams.catalog.domain.model.ProductStatus;

/**
 * Criteres de recherche de produits ; un critere null n'est pas applique.
 */
public record ProductCriteria(ProductStatus status, Long categoryId) {
}
