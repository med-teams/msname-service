package com.medteams.catalog.infrastructure.persistence;

import com.medteams.catalog.domain.model.ProductStatus;
import com.medteams.commons.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class ProductJpaEntity extends BaseEntity {

    @Column(nullable = false, length = 32, updatable = false)
    private String sku;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    // Reference par identifiant : la categorie est un autre agregat, chargee seulement si besoin.
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    protected ProductJpaEntity() {
    }

    ProductJpaEntity(String sku) {
        this.sku = sku;
    }

    void update(String name, String description, BigDecimal price, ProductStatus status, Long categoryId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.categoryId = categoryId;
    }

    public String getSku() {
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

    public ProductStatus getStatus() {
        return status;
    }

    public Long getCategoryId() {
        return categoryId;
    }
}
