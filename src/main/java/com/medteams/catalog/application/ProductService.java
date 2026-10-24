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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d'usage du catalogue produits. Orchestre le domaine et les ports de persistance ; les regles
 * metier elles-memes vivent dans {@link Product}.
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse create(CreateProductRequest request) {
        Sku sku = new Sku(request.sku());
        if (productRepository.existsBySku(sku)) {
            throw new ConflictException("Un produit avec le SKU " + sku + " existe deja");
        }
        requireCategory(request.categoryId());
        Product product = Product.create(sku, request.name(), request.description(), request.price(), request.categoryId());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(load(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(ProductStatus status, Long categoryId, int page, int size) {
        PageResult<Product> result = productRepository.search(new ProductCriteria(status, categoryId), page, size);
        return PageResponse.of(result.items(), page, size, result.totalElements()).map(ProductResponse::from);
    }

    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = load(id);
        if (!request.categoryId().equals(product.getCategoryId())) {
            requireCategory(request.categoryId());
        }
        product.updateDetails(request.name(), request.description(), request.price(), request.categoryId());
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse activate(Long id) {
        Product product = load(id);
        product.activate();
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse archive(Long id) {
        Product product = load(id);
        product.archive();
        return ProductResponse.from(productRepository.save(product));
    }

    private Product load(Long id) {
        return productRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Produit", id));
    }

    // Une categorie inexistante dans le corps de la requete est une donnee invalide (422), pas une ressource absente (404).
    private void requireCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException("Categorie inexistante : " + categoryId);
        }
    }
}
