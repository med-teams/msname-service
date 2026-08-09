package com.example.msname.domain.repository;

import com.example.msname.domain.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface) : le domaine definit le contrat, l'infrastructure l'implemente.
 */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findAll();
}
