package com.nikolaichuk.cdr.cdrbillingservice.domain.rating;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;

import java.math.RoundingMode;

/**
 * Rating algorithm for a single {@link UsageType}.
 */
public interface RatingStrategy {

    int CALCULATION_SCALE = 8;
    RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    UsageType supportedType();

    RatedCharge rate(RatingContext context);
}
