package com.medteams.catalog.domain.repository;

import com.medteams.catalog.domain.model.Product;
import com.medteams.catalog.domain.model.Sku;

import java.util.Optional;

/**
 * Port de persistance des produits : le domaine definit le contrat, l'infrastructure l'implemente.
 */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    boolean existsBySku(Sku sku);

    /**
     * Produits correspondant aux criteres, du plus recent au plus ancien.
     */
    PageResult<Product> search(ProductCriteria criteria, int page, int size);
}
