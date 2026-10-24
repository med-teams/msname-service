package com.medteams.catalog.application;

import com.medteams.catalog.application.dto.CategoryResponse;
import com.medteams.catalog.application.dto.CreateCategoryRequest;
import com.medteams.catalog.domain.model.Category;
import com.medteams.catalog.domain.repository.CategoryRepository;
import com.medteams.commons.exception.ConflictException;
import com.medteams.commons.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cas d'usage des categories de produits.
 */
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse create(CreateCategoryRequest request) {
        Category category = Category.create(request.name(), request.description());
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new ConflictException("La categorie '" + category.getName() + "' existe deja");
        }
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("Categorie", id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }
}
