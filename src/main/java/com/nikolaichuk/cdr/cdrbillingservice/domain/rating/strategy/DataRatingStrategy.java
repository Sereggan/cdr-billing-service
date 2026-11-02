package com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatedCharge;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatingContext;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatingStrategy;

import java.math.BigDecimal;

/**
 * Rates data: MB billed as GB (1 GB = 1024 MB), no free quota.
 */
public class DataRatingStrategy implements RatingStrategy {

    private static final BigDecimal MB_PER_GB = BigDecimal.valueOf(1024);

    @Override
    public UsageType supportedType() {
        return UsageType.DATA;
    }

    @Override
    public RatedCharge rate(RatingContext context) {
        BigDecimal megabytes = BigDecimal.valueOf(context.quantity());
        BigDecimal cost = context.plan().dataRate()
                .multiply(megabytes)
                .divide(MB_PER_GB, CALCULATION_SCALE, MONEY_ROUNDING);
        return RatedCharge.of(cost);
    }
}
