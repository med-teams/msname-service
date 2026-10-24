package com.medteams.catalog.domain.model;

import com.medteams.commons.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SkuTest {

    @Test
    void shouldNormalizeToUpperCase() {
        assertEquals("KB-001", new Sku("  kb-001 ").value());
    }

    @Test
    void shouldCompareByValue() {
        assertEquals(new Sku("kb-001"), new Sku("KB-001"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "ab", "-ABC", "AB CD", "AB_CD", "A23456789012345678901234567890123"})
    void shouldRejectInvalidSku(String value) {
        assertThrows(BusinessException.class, () -> new Sku(value));
    }
}
