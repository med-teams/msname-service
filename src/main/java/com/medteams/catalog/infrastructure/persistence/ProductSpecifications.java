package com.medteams.catalog.infrastructure.persistence;

import com.medteams.catalog.domain.model.ProductStatus;
import com.medteams.catalog.domain.repository.ProductCriteria;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduit les criteres de recherche du domaine en Specifications JPA combinables : ajouter un critere
 * revient a ajouter une methode, sans multiplier les requetes findByXAndY.
 */
final class ProductSpecifications {

    private ProductSpecifications() {
    }

    static Specification<ProductJpaEntity> matching(ProductCriteria criteria) {
        return Specification.where(hasStatus(criteria.status())).and(inCategory(criteria.categoryId()));
    }

    private static Specification<ProductJpaEntity> hasStatus(ProductStatus status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<ProductJpaEntity> inCategory(Long categoryId) {
        return categoryId == null ? null : (root, query, cb) -> cb.equal(root.get("categoryId"), categoryId);
    }
}
