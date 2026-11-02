package com.nikolaichuk.cdr.cdrbillingservice.domain.customer;

import com.nikolaichuk.cdr.cdrbillingservice.domain.shared.ValidationUtils;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * SubscriptionPlan value object.
 */
public record SubscriptionPlan(
        Long id,
        PlanType type,
        int freeCallMinutes,
        BigDecimal callRate,
        int freeSms,
        BigDecimal smsRate,
        BigDecimal dataRate
) {
    public SubscriptionPlan {
        ValidationUtils.requireNonNull(type, "type");
        ValidationUtils.requireNonNegative(freeCallMinutes, "freeCallMinutes");
        ValidationUtils.requireNonNegative(callRate, "callRate");
        ValidationUtils.requireNonNegative(freeSms, "freeSms");
        ValidationUtils.requireNonNegative(smsRate, "smsRate");
        ValidationUtils.requireNonNegative(dataRate, "dataRate");
    }

    /**
     * The free call allowance is granted in minutes but consumed in seconds, so the remainder is
     * derived here to keep the conversion in a single place.
     */
    public int remainingFreeSeconds(int usedFreeSeconds) {
        long grantedSeconds = Duration.ofMinutes(freeCallMinutes).toSeconds();
        return (int) Math.max(0, grantedSeconds - usedFreeSeconds);
    }

    /**
     * The remaining allowance floored to whole minutes, as reported to the customer.
     */
    public int remainingFreeMinutes(int usedFreeSeconds) {
        return (int) Duration.ofSeconds(remainingFreeSeconds(usedFreeSeconds)).toMinutes();
    }

    public int remainingFreeSms(int usedFreeSms) {
        return Math.max(0, freeSms - usedFreeSms);
    }
}
