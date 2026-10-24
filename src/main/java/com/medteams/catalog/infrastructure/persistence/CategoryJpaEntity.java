package com.medteams.catalog.infrastructure.persistence;

import com.medteams.commons.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class CategoryJpaEntity extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 500)
    private String description;

    protected CategoryJpaEntity() {
    }

    CategoryJpaEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
