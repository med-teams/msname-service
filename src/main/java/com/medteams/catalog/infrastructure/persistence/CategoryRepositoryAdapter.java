package com.medteams.catalog.infrastructure.persistence;

import com.medteams.catalog.domain.model.Category;
import com.medteams.catalog.domain.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter de sortie : implemente le port des categories avec Spring Data JPA.
 */
@Repository
class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    CategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Category save(Category category) {
        CategoryJpaEntity saved = jpaRepository.saveAndFlush(new CategoryJpaEntity(category.getName(), category.getDescription()));
        return toDomain(saved);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return jpaRepository.findById(id).map(CategoryRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return jpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll(Sort.by("name")).stream().map(CategoryRepositoryAdapter::toDomain).toList();
    }

    private static Category toDomain(CategoryJpaEntity entity) {
        return Category.restore(entity.getId(), entity.getName(), entity.getDescription());
    }
}
