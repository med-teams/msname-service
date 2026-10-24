package com.medteams.catalog.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation OpenAPI : /v3/api-docs (JSON) et /swagger-ui.html (interface).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Catalog Service API")
                .version("v1")
                .description("Catalogue produits du SaaS : categories, produits et cycle de vie"));
    }
}
