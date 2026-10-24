package com.medteams.catalog.domain.model;

import com.medteams.commons.exception.BusinessException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Reference unique d'un produit (Stock Keeping Unit). Value object : immuable, compare par valeur et
 * normalise en majuscules, donc "kb-001" et "KB-001" designent le meme produit.
 */
public record Sku(String value) {

    private static final Pattern FORMAT = Pattern.compile("[A-Z0-9][A-Z0-9-]{2,31}");

    public Sku {
        if (value == null) {
            throw new BusinessException("Le SKU est obligatoire");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches()) {
            throw new BusinessException("SKU invalide '" + value + "' : 3 a 32 caracteres, lettres, chiffres et tirets");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
