package com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatedCharge;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatingContext;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatingStrategy;

import java.math.BigDecimal;

/**
 * Rates calls based on the subscription plan's call rate and free call minutes.
 */
public class CallRatingStrategy implements RatingStrategy {

    private static final int SECONDS_PER_MINUTE = 60;

    @Override
    public UsageType supportedType() {
        return UsageType.CALL;
    }

    @Override
    public RatedCharge rate(RatingContext context) {
        SubscriptionPlan plan = context.plan();
        int usedSeconds = context.quantity();
        int freeApplied = Math.min(usedSeconds, plan.remainingFreeSeconds(context.usedFreeSeconds()));
        int billable = usedSeconds - freeApplied;
        BigDecimal cost = plan.callRate()
                .multiply(BigDecimal.valueOf(billable))
                .divide(BigDecimal.valueOf(SECONDS_PER_MINUTE), CALCULATION_SCALE, MONEY_ROUNDING);
        return new RatedCharge(cost, freeApplied, 0);
    }
}
