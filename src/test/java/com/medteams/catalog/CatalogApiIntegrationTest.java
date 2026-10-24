package com.medteams.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Application complete (Flyway, JPA, web, gestion d'erreurs) sur une base H2 en memoire.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CatalogApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long idOf(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).at("/data/id").asLong();
    }

    @Test
    void shouldManageProductLifecycleEndToEnd() throws Exception {
        long categoryId = idOf(mockMvc.perform(post("/api/v1/categories").contentType(APPLICATION_JSON)
                        .content("""
                                {"name": "Peripheriques", "description": "Claviers et souris"}
                                """))
                .andExpect(status().isCreated())
                .andReturn());

        String product = """
                {"sku": "%s", "name": "Clavier", "price": 49.90, "categoryId": %d}
                """;
        long productId = idOf(mockMvc.perform(post("/api/v1/products").contentType(APPLICATION_JSON)
                        .content(product.formatted("kb-100", categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sku").value("KB-100"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn());

        // Meme SKU, casse differente : refuse
        mockMvc.perform(post("/api/v1/products").contentType(APPLICATION_JSON).content(product.formatted("KB-100", categoryId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        mockMvc.perform(post("/api/v1/products/{id}/activate", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(get("/api/v1/products").param("status", "ACTIVE").param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(productId));

        mockMvc.perform(post("/api/v1/products/{id}/archive", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));

        // Un produit archive ne se modifie plus
        mockMvc.perform(put("/api/v1/products/{id}", productId).contentType(APPLICATION_JSON)
                        .content("""
                                {"name": "Clavier v2", "price": 59.90, "categoryId": %d}
                                """.formatted(categoryId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void shouldReferenceExistingCategoryOnly() throws Exception {
        mockMvc.perform(post("/api/v1/products").contentType(APPLICATION_JSON)
                        .content("""
                                {"sku": "XX-001", "name": "Orphelin", "price": 10, "categoryId": 999999}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Categorie inexistante : 999999"));
    }

    @Test
    void shouldReturnStandardErrorForUnknownRoute() throws Exception {
        mockMvc.perform(get("/api/v1/inconnu"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/v1/inconnu"));
    }

    @Test
    void shouldExposeHealthAndApiDocumentation() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Catalog Service API"));
    }
}
