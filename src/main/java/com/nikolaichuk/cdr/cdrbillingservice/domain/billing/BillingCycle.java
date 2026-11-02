package com.nikolaichuk.cdr.cdrbillingservice.domain.billing;

import com.nikolaichuk.cdr.cdrbillingservice.domain.shared.ValidationUtils;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;

/**
 * BillingCycle value object (calendar month, UTC).
 */
public record BillingCycle(YearMonth value) {

    public BillingCycle {
        ValidationUtils.requireNonNull(value, "value");
    }

    public static BillingCycle from(OffsetDateTime timestamp) {
        ValidationUtils.requireNonNull(timestamp, "timestamp");
        return new BillingCycle(YearMonth.from(timestamp.withOffsetSameInstant(ZoneOffset.UTC)));
    }
}
