package com.nikolaichuk.cdr.cdrbillingservice.domain.rating;

import com.nikolaichuk.cdr.cdrbillingservice.domain.shared.ValidationUtils;

import java.math.BigDecimal;

/**
 * Output of a {@link RatingStrategy}: cost plus free quota consumed.
 */
public record RatedCharge(BigDecimal cost, int freeSecondsUsed, int freeSmsUsed) {

    public RatedCharge {
        ValidationUtils.requireNonNegative(cost, "cost");
        ValidationUtils.requireNonNegative(freeSecondsUsed, "freeSecondsUsed");
        ValidationUtils.requireNonNegative(freeSmsUsed, "freeSmsUsed");
    }

    public static RatedCharge of(BigDecimal cost) {
        return new RatedCharge(cost, 0, 0);
    }
}
