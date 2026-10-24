package com.medteams.catalog.domain.model;

import com.medteams.commons.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Produit du catalogue. Entite de domaine pure (aucune annotation JPA ni Spring) qui porte toutes les
 * regles metier : un Product ne peut jamais se trouver dans un etat invalide.
 */
public class Product {

    private static final int NAME_MAX_LENGTH = 120;
    private static final int DESCRIPTION_MAX_LENGTH = 1000;

    private final Long id;
    private final Sku sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private ProductStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Product(Long id, Sku sku, ProductStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sku = sku;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Nouveau produit : toujours cree en brouillon, il doit etre active pour etre visible des clients.
     */
    public static Product create(Sku sku, String name, String description, BigDecimal price, Long categoryId) {
        Product product = new Product(null, sku, ProductStatus.DRAFT, null, null);
        product.applyDetails(name, description, price, categoryId);
        return product;
    }

    /**
     * Reconstitue un produit deja persiste ; reserve a l'adapter de persistance.
     */
    public static Product restore(Long id, Sku sku, String name, String description, BigDecimal price,
                                  Long categoryId, ProductStatus status, Instant createdAt, Instant updatedAt) {
        Product product = new Product(id, sku, status, createdAt, updatedAt);
        product.name = name;
        product.description = description;
        product.price = price;
        product.categoryId = categoryId;
        return product;
    }

    public void updateDetails(String name, String description, BigDecimal price, Long categoryId) {
        if (status == ProductStatus.ARCHIVED) {
            throw new BusinessException("Le produit " + sku + " est archive : il ne peut plus etre modifie");
        }
        applyDetails(name, description, price, categoryId);
    }

    public void activate() {
        transitionTo(ProductStatus.ACTIVE);
    }

    public void archive() {
        transitionTo(ProductStatus.ARCHIVED);
    }

    private void transitionTo(ProductStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new BusinessException("Transition " + status + " -> " + target + " interdite pour le produit " + sku);
        }
        status = target;
    }

    private void applyDetails(String name, String description, BigDecimal price, Long categoryId) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Le nom du produit est obligatoire");
        }
        if (name.trim().length() > NAME_MAX_LENGTH) {
            throw new BusinessException("Le nom du produit depasse " + NAME_MAX_LENGTH + " caracteres");
        }
        if (description != null && description.trim().length() > DESCRIPTION_MAX_LENGTH) {
            throw new BusinessException("La description depasse " + DESCRIPTION_MAX_LENGTH + " caracteres");
        }
        if (price == null || price.signum() <= 0) {
            throw new BusinessException("Le prix doit etre superieur a zero");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw new BusinessException("Le prix ne peut pas avoir plus de 2 decimales");
        }
        if (categoryId == null) {
            throw new BusinessException("La categorie du produit est obligatoire");
        }
        this.name = name.trim();
        this.description = description == null || description.isBlank() ? null : description.trim();
        this.price = price;
        this.categoryId = categoryId;
    }

    public Long getId() {
        return id;
    }

    public Sku getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
