package com.example.msname.infrastructure.web;

import com.example.commons.api.dto.ApiResponse;
import com.example.msname.application.ProductService;
import com.example.msname.application.dto.ProductRequest;
import com.example.msname.application.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private ProductController productController;

    @BeforeEach
    void setUp() {
        productController = new ProductController(productService);
    }

    @Test
    void shouldCreateProduct() {
        // Given
        ProductRequest request = new ProductRequest();

        ProductResponse response = new ProductResponse(
                1L,
                "Product 1",
                new BigDecimal("100.00")
        );

        when(productService.create(request)).thenReturn(response);

        // When
        ApiResponse<ProductResponse> result =
                productController.create(request);

        // Then
        assertNotNull(result);
        verify(productService).create(request);
    }

    @Test
    void shouldGetProductById() {
        // Given
        Long productId = 1L;

        ProductResponse response = new ProductResponse(
                productId,
                "Product 1",
                new BigDecimal("100.00")
        );

        when(productService.getById(productId)).thenReturn(response);

        // When
        ApiResponse<ProductResponse> result =
                productController.getById(productId);

        // Then
        assertNotNull(result);
        verify(productService).getById(productId);
    }

    @Test
    void shouldGetAllProducts() {
        // Given
        ProductResponse product1 = new ProductResponse(
                1L,
                "Product 1",
                new BigDecimal("100.00")
        );

        ProductResponse product2 = new ProductResponse(
                2L,
                "Product 2",
                new BigDecimal("200.00")
        );

        List<ProductResponse> products = List.of(product1, product2);

        when(productService.getAll()).thenReturn(products);

        // When
        ApiResponse<List<ProductResponse>> result =
                productController.getAll();

        // Then
        assertNotNull(result);
        verify(productService).getAll();
    }
}