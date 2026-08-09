package com.example.msname.domain.service;

import com.example.commons.api.exception.BusinessException;
import com.example.msname.domain.model.Product;

import java.math.BigDecimal;

/**
 * Regles metier pures, independantes de toute techno (Spring, JPA, HTTP...).
 */
public class ProductDomainService {

    public void validateForCreation(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new BusinessException("PRODUCT_NAME_REQUIRED", "Le nom du produit est obligatoire");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("PRODUCT_PRICE_INVALID", "Le prix doit etre superieur a zero");
        }
    }
}
