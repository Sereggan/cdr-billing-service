package com.nikolaichuk.cdr.cdrbillingservice.domain.shared;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Domain validation helpers.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static <T> T requireNonNull(T value, String field) {
        return Objects.requireNonNull(value, field + " must not be null");
    }

    public static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    public static int requireNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must not be negative");
        }
        return value;
    }

    public static long requireNonNegative(long value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must not be negative");
        }
        return value;
    }

    public static BigDecimal requireNonNegative(BigDecimal value, String field) {
        requireNonNull(value, field);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " must not be negative");
        }
        return value;
    }
}
