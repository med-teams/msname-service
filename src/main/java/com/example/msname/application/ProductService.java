package com.example.msname.application;

import com.example.commons.api.exception.ResourceNotFoundException;
import com.example.msname.application.dto.ProductRequest;
import com.example.msname.application.dto.ProductResponse;
import com.example.msname.domain.model.Product;
import com.example.msname.domain.repository.ProductRepository;
import com.example.msname.domain.service.ProductDomainService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestre les cas d'usage : appelle le domaine, puis le port de persistance.
 * Ne contient aucune logique metier elle-meme : ca vit dans ProductDomainService.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductDomainService productDomainService;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.productDomainService = new ProductDomainService();
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product(null, request.getName(), request.getPrice());
        productDomainService.validateForCreation(product);
        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable: " + id));
        return toResponse(product);
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice());
    }
}
