package com.example.msname.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UtilitaireTest {

    @Test
    void doitAdditionner() {
        assertEquals(5, Utilitaire.additionner(2, 3));
    }

    @Test
    void doitSoustraire() {
        assertEquals(2, Utilitaire.soustraire(5, 3));
    }

    @Test
    void doitMultiplier() {
        assertEquals(15, Utilitaire.multiplier(3, 5));
    }

    @Test
    void doitDiviser() {
        assertEquals(2.5, Utilitaire.diviser(5, 2));
    }

    @Test
    void doitRefuserDivisionParZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Utilitaire.diviser(5, 0)
        );
    }
}