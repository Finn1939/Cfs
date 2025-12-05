package com.finn1939.cfs.util;

public final class InputUtils {
    private InputUtils() {}

    /**
     * Parse a quantity input string. Rules:
     * - If input equals (case-insensitive) "all", return availableQuantity.
     * - If input is a number, parse as integer. "0" is treated as zero (not "all").
     * - Throws IllegalArgumentException for invalid values or negative numbers.
     */
    public static int parseQuantity(String raw, int availableQuantity) {
        if (raw == null) throw new IllegalArgumentException("Quantity required");
        String v = raw.trim();
        if (v.equalsIgnoreCase("all")) {
            return availableQuantity;
        }
        try {
            int q = Integer.parseInt(v);
            if (q < 0) throw new IllegalArgumentException("Quantity cannot be negative");
            return q; // 0 is valid and means zero
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid quantity: " + raw);
        }
    }
}