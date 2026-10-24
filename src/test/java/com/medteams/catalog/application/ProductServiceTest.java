package com.medteams.catalog.application;

import com.medteams.catalog.application.dto.CreateProductRequest;
import com.medteams.catalog.application.dto.ProductResponse;
import com.medteams.catalog.application.dto.UpdateProductRequest;
import com.medteams.catalog.domain.model.Product;
import com.medteams.catalog.domain.model.ProductStatus;
import com.medteams.catalog.domain.model.Sku;
import com.medteams.catalog.domain.repository.CategoryRepository;
import com.medteams.catalog.domain.repository.PageResult;
import com.medteams.catalog.domain.repository.ProductCriteria;
import com.medteams.catalog.domain.repository.ProductRepository;
import com.medteams.commons.exception.BusinessException;
import com.medteams.commons.exception.ConflictException;
import com.medteams.commons.exception.ResourceNotFoundException;
import com.medteams.commons.response.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private static final CreateProductRequest CREATE_REQUEST =
            new CreateProductRequest("kb-001", "Clavier", "Clavier mecanique", new BigDecimal("29.90"), 1L);

    // Simule la base : renvoie le produit avec un id et des dates d'audit
    private static Product persisted(Product product, Long id) {
        Instant now = Instant.now();
        return Product.restore(id, product.getSku(), product.getName(), product.getDescription(), product.getPrice(),
                product.getCategoryId(), product.getStatus(), now, now);
    }

    @Test
    void shouldCreateDraftProduct() {
        // Given
        when(productRepository.existsBySku(new Sku("KB-001"))).thenReturn(false);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.save(any())).thenAnswer(invocation -> persisted(invocation.getArgument(0), 42L));

        // When
        ProductResponse response = productService.create(CREATE_REQUEST);

        // Then
        assertEquals(42L, response.id());
        assertEquals("KB-001", response.sku());
        assertEquals(ProductStatus.DRAFT, response.status());
    }

    @Test
    void shouldRejectDuplicateSku() {
        // Given
        when(productRepository.existsBySku(new Sku("KB-001"))).thenReturn(true);

        // When / Then
        assertThrows(ConflictException.class, () -> productService.create(CREATE_REQUEST));
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnknownCategory() {
        // Given
        when(productRepository.existsBySku(new Sku("KB-001"))).thenReturn(false);
        when(categoryRepository.existsById(1L)).thenReturn(false);

        // When / Then
        BusinessException exception = assertThrows(BusinessException.class, () -> productService.create(CREATE_REQUEST));
        assertEquals("Categorie inexistante : 1", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundForUnknownProduct() {
        // Given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> productService.get(99L));
        assertEquals("Produit introuvable : 99", exception.getMessage());
    }

    @Test
    void shouldActivateProduct() {
        // Given
        Product draft = persisted(Product.create(new Sku("KB-001"), "Clavier", null, BigDecimal.TEN, 1L), 42L);
        when(productRepository.findById(42L)).thenReturn(Optional.of(draft));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProductResponse response = productService.activate(42L);

        // Then
        assertEquals(ProductStatus.ACTIVE, response.status());
        verify(productRepository).save(draft);
    }

    @Test
    void shouldCheckNewCategoryOnUpdate() {
        // Given
        Product existing = persisted(Product.create(new Sku("KB-001"), "Clavier", null, BigDecimal.TEN, 1L), 42L);
        when(productRepository.findById(42L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsById(2L)).thenReturn(false);
        UpdateProductRequest request = new UpdateProductRequest("Clavier", null, BigDecimal.TEN, 2L);

        // When / Then
        assertThrows(BusinessException.class, () -> productService.update(42L, request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldMapSearchResultToPage() {
        // Given
        Product product = persisted(Product.create(new Sku("KB-001"), "Clavier", null, BigDecimal.TEN, 1L), 42L);
        when(productRepository.search(new ProductCriteria(ProductStatus.DRAFT, 1L), 2, 10))
                .thenReturn(new PageResult<>(List.of(product), 21));

        // When
        PageResponse<ProductResponse> page = productService.search(ProductStatus.DRAFT, 1L, 2, 10);

        // Then
        assertEquals(1, page.content().size());
        assertEquals(42L, page.content().get(0).id());
        assertEquals(21, page.totalElements());
        assertEquals(3, page.totalPages());
    }
}
