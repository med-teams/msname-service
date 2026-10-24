package com.medteams.catalog.domain.repository;

import java.util.List;

/**
 * Une page d'elements et le nombre total d'elements correspondant a la recherche.
 */
public record PageResult<T>(List<T> items, long totalElements) {

    public PageResult {
        items = List.copyOf(items);
    }
}
