package com.example.msname.infrastructure.web;

import com.example.commons.api.dto.ApiResponse;
import com.example.msname.application.ProductService;
import com.example.msname.application.dto.ProductRequest;
import com.example.msname.application.dto.ProductResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ApiResponse<ProductResponse> create(@RequestBody ProductRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(productService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> getAll() {
        return ApiResponse.ok(productService.getAll());
    }
}
