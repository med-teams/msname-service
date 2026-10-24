package com.medteams.catalog.infrastructure.persistence;

import com.medteams.catalog.domain.model.Category;
import com.medteams.catalog.domain.model.Product;
import com.medteams.catalog.domain.model.ProductStatus;
import com.medteams.catalog.domain.model.Sku;
import com.medteams.catalog.domain.repository.CategoryRepository;
import com.medteams.catalog.domain.repository.PageResult;
import com.medteams.catalog.domain.repository.ProductCriteria;
import com.medteams.catalog.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Adapters JPA sur une vraie base H2 dont le schema est cree par les migrations Flyway
 * (Hibernate en mode validate : le test echoue si entites et migrations divergent).
 */
@DataJpaTest
@Import({ProductRepositoryAdapter.class, CategoryRepositoryAdapter.class})
class ProductRepositoryAdapterTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long peripherals;
    private Long screens;

    @BeforeEach
    void createCategories() {
        peripherals = categoryRepository.save(Category.create("Peripheriques", null)).getId();
        screens = categoryRepository.save(Category.create("Ecrans", null)).getId();
    }

    private Product newProduct(String sku, Long categoryId) {
        return Product.create(new Sku(sku), "Produit " + sku, null, new BigDecimal("19.99"), categoryId);
    }

    @Test
    void shouldPersistNewProductWithAuditFields() {
        // When
        Product saved = productRepository.save(newProduct("KB-001", peripherals));

        // Then
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        Product reloaded = productRepository.findById(saved.getId()).orElseThrow();
        assertEquals("KB-001", reloaded.getSku().value());
        assertEquals(new BigDecimal("19.99"), reloaded.getPrice());
        assertEquals(ProductStatus.DRAFT, reloaded.getStatus());
    }

    @Test
    void shouldUpdateExistingProduct() {
        // Given
        Product saved = productRepository.save(newProduct("KB-001", peripherals));

        // When
        saved.activate();
        Product updated = productRepository.save(saved);

        // Then
        assertEquals(saved.getId(), updated.getId());
        assertEquals(ProductStatus.ACTIVE, updated.getStatus());
        assertEquals(saved.getCreatedAt(), updated.getCreatedAt());
        assertFalse(updated.getUpdatedAt().isBefore(updated.getCreatedAt()));
    }

    @Test
    void shouldSearchByCriteriaNewestFirst() {
        // Given
        Product first = productRepository.save(newProduct("KB-001", peripherals));
        Product second = productRepository.save(newProduct("KB-002", peripherals));
        productRepository.save(newProduct("KB-003", peripherals));
        productRepository.save(newProduct("SC-001", screens));
        for (Product product : List.of(first, second)) {
            product.activate();
            productRepository.save(product);
        }

        // When
        PageResult<Product> active = productRepository.search(new ProductCriteria(ProductStatus.ACTIVE, peripherals), 0, 10);
        PageResult<Product> firstPage = productRepository.search(new ProductCriteria(null, null), 0, 2);

        // Then
        assertEquals(List.of("KB-002", "KB-001"), active.items().stream().map(p -> p.getSku().value()).toList());
        assertEquals(2, active.totalElements());
        assertEquals(2, firstPage.items().size());
        assertEquals(4, firstPage.totalElements());
    }

    @Test
    void shouldFindSkuWhateverItsCase() {
        // Given
        productRepository.save(newProduct("KB-001", peripherals));

        // Then
        assertTrue(productRepository.existsBySku(new Sku("kb-001")));
        assertFalse(productRepository.existsBySku(new Sku("KB-999")));
    }

    @Test
    void shouldRejectDuplicateSkuAtDatabaseLevel() {
        // Given
        productRepository.save(newProduct("KB-001", peripherals));

        // When / Then
        assertThrows(DataIntegrityViolationException.class, () -> productRepository.save(newProduct("KB-001", screens)));
    }
}
