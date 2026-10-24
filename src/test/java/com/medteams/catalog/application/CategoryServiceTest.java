package com.medteams.catalog.application;

import com.medteams.catalog.application.dto.CategoryResponse;
import com.medteams.catalog.application.dto.CreateCategoryRequest;
import com.medteams.catalog.domain.model.Category;
import com.medteams.catalog.domain.repository.CategoryRepository;
import com.medteams.commons.exception.ConflictException;
import com.medteams.commons.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldCreateCategory() {
        // Given
        when(categoryRepository.existsByNameIgnoreCase("Peripheriques")).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            return Category.restore(7L, category.getName(), category.getDescription());
        });

        // When
        CategoryResponse response = categoryService.create(new CreateCategoryRequest(" Peripheriques ", null));

        // Then
        assertEquals(7L, response.id());
        assertEquals("Peripheriques", response.name());
    }

    @Test
    void shouldRejectDuplicateName() {
        // Given
        when(categoryRepository.existsByNameIgnoreCase("Peripheriques")).thenReturn(true);

        // When / Then
        assertThrows(ConflictException.class, () -> categoryService.create(new CreateCategoryRequest("Peripheriques", null)));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundForUnknownCategory() {
        // Given
        when(categoryRepository.findById(5L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ResourceNotFoundException.class, () -> categoryService.get(5L));
    }
}
