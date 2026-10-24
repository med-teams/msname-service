package com.medteams.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    boolean existsByNameIgnoreCase(String name);
}
