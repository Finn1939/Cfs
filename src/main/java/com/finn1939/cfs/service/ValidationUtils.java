package com.finn1939.cfs.service;

public final class ValidationUtils {
    private ValidationUtils() {}

    public static void requireNonNull(Object o, String message) {
        if (o == null) throw new IllegalArgumentException(message);
    }

    public static void requirePositive(int v, String message) {
        if (v <= 0) throw new IllegalArgumentException(message);
    }
}