package com.medteams.catalog.domain.repository;

import com.medteams.catalog.domain.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance des categories.
 */
public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(Long id);

    boolean existsById(Long id);

    boolean existsByNameIgnoreCase(String name);

    /**
     * Toutes les categories, triees par nom.
     */
    List<Category> findAll();
}
