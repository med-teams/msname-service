package com.medteams.catalog.infrastructure.persistence;

import com.medteams.catalog.domain.model.Product;
import com.medteams.catalog.domain.model.Sku;
import com.medteams.catalog.domain.repository.PageResult;
import com.medteams.catalog.domain.repository.ProductCriteria;
import com.medteams.catalog.domain.repository.ProductRepository;
import com.medteams.commons.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter de sortie : implemente le port du domaine avec Spring Data JPA.
 */
@Repository
class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = product.getId() == null
                ? new ProductJpaEntity(product.getSku().value())
                : jpaRepository.findById(product.getId()).orElseThrow(() -> ResourceNotFoundException.of("Produit", product.getId()));
        entity.update(product.getName(), product.getDescription(), product.getPrice(), product.getStatus(), product.getCategoryId());
        // flush : dates d'audit et version a jour dans la reponse, contraintes d'unicite verifiees immediatement
        return toDomain(jpaRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).map(ProductRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsBySku(Sku sku) {
        return jpaRepository.existsBySku(sku.value());
    }

    @Override
    public PageResult<Product> search(ProductCriteria criteria, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<ProductJpaEntity> result = jpaRepository.findAll(ProductSpecifications.matching(criteria), pageRequest);
        return new PageResult<>(result.map(ProductRepositoryAdapter::toDomain).getContent(), result.getTotalElements());
    }

    private static Product toDomain(ProductJpaEntity entity) {
        return Product.restore(entity.getId(), new Sku(entity.getSku()), entity.getName(), entity.getDescription(),
                entity.getPrice(), entity.getCategoryId(), entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
