package com.example.msname.utils;

public final class Utilitaire {
    private Utilitaire() {
    }

    public static int additionner(int a, int b) {
        return a + b;
    }

    public static int soustraire(int a, int b) {
        return a - b;
    }

    public static int multiplier(int a, int b) {
        return a * b;
    }

    public static double diviser(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Le diviseur ne peut pas être zéro.");
        }
        return a / b;
    }
}
