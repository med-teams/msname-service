package com.example.msname.infrastructure.web;

import com.example.commons.api.dto.ApiResponse;
import com.example.msname.application.ProductService;
import com.example.msname.application.dto.ProductRequest;
import com.example.msname.application.dto.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ApiResponse<ProductResponse> create(@RequestBody ProductRequest request) {
        log.info("Received request to create product: {}", request);
        return ApiResponse.ok(productService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getById(@PathVariable Long id) {
        log.info("Received request to get product with ID: {}", id);
        log.debug("Fetching product details for ID: {}", id);
        return ApiResponse.ok(productService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> getAll() {
        log.info("Received request to get all products");
        return ApiResponse.ok(productService.getAll());
    }
}
