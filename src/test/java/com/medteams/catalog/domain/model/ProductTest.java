package com.medteams.catalog.domain.model;

import com.medteams.commons.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private static Product draft() {
        return Product.create(new Sku("KB-001"), "Clavier", "Clavier mecanique", new BigDecimal("29.90"), 1L);
    }

    @Test
    void shouldCreateProductAsDraft() {
        // When
        Product product = draft();

        // Then
        assertEquals(ProductStatus.DRAFT, product.getStatus());
        assertNull(product.getId());
        assertEquals("KB-001", product.getSku().value());
    }

    @Test
    void shouldTrimNameAndDropBlankDescription() {
        // When
        Product product = Product.create(new Sku("KB-001"), "  Clavier  ", "   ", new BigDecimal("10"), 1L);

        // Then
        assertEquals("Clavier", product.getName());
        assertNull(product.getDescription());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "10.999"})
    void shouldRejectInvalidPrice(String price) {
        assertThrows(BusinessException.class,
                () -> Product.create(new Sku("KB-001"), "Clavier", null, new BigDecimal(price), 1L));
    }

    @Test
    void shouldAcceptPriceWithTrailingZeros() {
        assertDoesNotThrow(() -> Product.create(new Sku("KB-001"), "Clavier", null, new BigDecimal("10.500"), 1L));
    }

    @Test
    void shouldRejectBlankNameAndMissingCategory() {
        assertThrows(BusinessException.class, () -> Product.create(new Sku("KB-001"), " ", null, BigDecimal.ONE, 1L));
        assertThrows(BusinessException.class, () -> Product.create(new Sku("KB-001"), "Clavier", null, BigDecimal.ONE, null));
    }

    @Test
    void shouldActivateThenArchive() {
        // Given
        Product product = draft();

        // When
        product.activate();
        product.archive();

        // Then
        assertEquals(ProductStatus.ARCHIVED, product.getStatus());
    }

    @Test
    void shouldNotReactivateArchivedProduct() {
        // Given
        Product product = draft();
        product.archive();

        // When
        BusinessException exception = assertThrows(BusinessException.class, product::activate);

        // Then
        assertEquals("Transition ARCHIVED -> ACTIVE interdite pour le produit KB-001", exception.getMessage());
    }

    @Test
    void shouldNotUpdateArchivedProduct() {
        // Given
        Product product = draft();
        product.archive();

        // Then
        assertThrows(BusinessException.class, () -> product.updateDetails("Autre", null, BigDecimal.TEN, 1L));
    }

    @Test
    void shouldUpdateDetailsOfActiveProduct() {
        // Given
        Product product = draft();
        product.activate();

        // When
        product.updateDetails("Clavier sans fil", "Bluetooth", new BigDecimal("39.90"), 2L);

        // Then
        assertEquals("Clavier sans fil", product.getName());
        assertEquals(new BigDecimal("39.90"), product.getPrice());
        assertEquals(2L, product.getCategoryId());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }
}
