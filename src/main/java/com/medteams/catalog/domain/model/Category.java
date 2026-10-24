package com.medteams.catalog.domain.model;

import com.medteams.commons.exception.BusinessException;

/**
 * Categorie de produits. Entite de domaine pure.
 */
public class Category {

    private static final int NAME_MAX_LENGTH = 80;
    private static final int DESCRIPTION_MAX_LENGTH = 500;

    private final Long id;
    private final String name;
    private final String description;

    private Category(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static Category create(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Le nom de la categorie est obligatoire");
        }
        if (name.trim().length() > NAME_MAX_LENGTH) {
            throw new BusinessException("Le nom de la categorie depasse " + NAME_MAX_LENGTH + " caracteres");
        }
        if (description != null && description.trim().length() > DESCRIPTION_MAX_LENGTH) {
            throw new BusinessException("La description depasse " + DESCRIPTION_MAX_LENGTH + " caracteres");
        }
        String cleanDescription = description == null || description.isBlank() ? null : description.trim();
        return new Category(null, name.trim(), cleanDescription);
    }

    /**
     * Reconstitue une categorie deja persistee ; reserve a l'adapter de persistance.
     */
    public static Category restore(Long id, String name, String description) {
        return new Category(id, name, description);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
