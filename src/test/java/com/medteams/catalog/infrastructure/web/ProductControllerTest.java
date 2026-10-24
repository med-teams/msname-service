package com.medteams.catalog.infrastructure.web;

import com.medteams.catalog.application.ProductService;
import com.medteams.catalog.application.dto.ProductResponse;
import com.medteams.catalog.domain.model.ProductStatus;
import com.medteams.commons.exception.BusinessException;
import com.medteams.commons.exception.ResourceNotFoundException;
import com.medteams.commons.response.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Couche web seule : routes, validation des entrees et format des erreurs (GlobalExceptionHandler).
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private static ProductResponse sample(ProductStatus status) {
        Instant now = Instant.parse("2026-10-24T10:00:00Z");
        return new ProductResponse(42L, "KB-001", "Clavier", null, new BigDecimal("29.90"), status, 1L, now, now);
    }

    @Test
    void shouldCreateProductAndReturnLocation() throws Exception {
        // Given
        when(productService.create(any())).thenReturn(sample(ProductStatus.DRAFT));

        // When / Then
        mockMvc.perform(post("/api/v1/products").contentType(APPLICATION_JSON)
                        .content("""
                                {"sku": "kb-001", "name": "Clavier", "price": 29.90, "categoryId": 1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/products/42"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("KB-001"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void shouldRejectInvalidBodyWithFieldViolations() throws Exception {
        mockMvc.perform(post("/api/v1/products").contentType(APPLICATION_JSON)
                        .content("""
                                {"sku": "a", "name": " ", "price": -1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/v1/products"))
                .andExpect(jsonPath("$.violations[*].field").value(contains("categoryId", "name", "price", "sku")));
        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/products").contentType(APPLICATION_JSON).content("{pas du json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void shouldReturnNotFoundForUnknownProduct() throws Exception {
        // Given
        when(productService.get(99L)).thenThrow(ResourceNotFoundException.of("Produit", 99L));

        // When / Then
        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Produit introuvable : 99"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnUnprocessableEntityForForbiddenTransition() throws Exception {
        // Given
        when(productService.activate(42L)).thenThrow(new BusinessException("Transition ARCHIVED -> ACTIVE interdite"));

        // When / Then
        mockMvc.perform(post("/api/v1/products/42/activate"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void shouldSearchWithFilters() throws Exception {
        // Given
        when(productService.search(ProductStatus.ACTIVE, 1L, 0, 20))
                .thenReturn(PageResponse.of(List.of(sample(ProductStatus.ACTIVE)), 0, 20, 1));

        // When / Then
        mockMvc.perform(get("/api/v1/products").param("status", "ACTIVE").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));
    }

    @Test
    void shouldRejectPageSizeAboveLimit() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations[0].field").value("size"));
        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectUnknownStatusFilter() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("status", "INCONNU"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }
}
