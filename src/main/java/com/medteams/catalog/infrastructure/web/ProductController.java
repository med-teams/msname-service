package com.medteams.catalog.infrastructure.web;

import com.medteams.catalog.application.ProductService;
import com.medteams.catalog.application.dto.CreateProductRequest;
import com.medteams.catalog.application.dto.ProductResponse;
import com.medteams.catalog.application.dto.UpdateProductRequest;
import com.medteams.catalog.domain.model.ProductStatus;
import com.medteams.commons.response.ApiResponse;
import com.medteams.commons.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Produits", description = "Catalogue produits et cycle de vie DRAFT -> ACTIVE -> ARCHIVED")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Cree un produit en brouillon")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detail d'un produit")
    public ApiResponse<ProductResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.get(id));
    }

    @GetMapping
    @Operation(summary = "Recherche paginee, filtrable par statut et par categorie")
    public ApiResponse<PageResponse<ProductResponse>> search(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "doit etre >= 0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "doit etre >= 1") @Max(value = 100, message = "doit etre <= 100") int size) {
        return ApiResponse.ok(productService.search(status, categoryId, page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifie un produit (interdit s'il est archive)")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return ApiResponse.ok(productService.update(id, request));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Publie un produit brouillon")
    public ApiResponse<ProductResponse> activate(@PathVariable Long id) {
        return ApiResponse.ok(productService.activate(id));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive un produit (definitif)")
    public ApiResponse<ProductResponse> archive(@PathVariable Long id) {
        return ApiResponse.ok(productService.archive(id));
    }
}
