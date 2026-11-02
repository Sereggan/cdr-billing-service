package com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatedCharge;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatingContext;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatingStrategy;

import java.math.BigDecimal;

/**
 * Rates SMS by count, free messages applied first.
 */
public class SmsRatingStrategy implements RatingStrategy {

    @Override
    public UsageType supportedType() {
        return UsageType.SMS;
    }

    @Override
    public RatedCharge rate(RatingContext context) {
        SubscriptionPlan plan = context.plan();
        int used = context.quantity();
        int freeApplied = Math.min(used, plan.remainingFreeSms(context.usedFreeSms()));
        int billable = used - freeApplied;
        BigDecimal cost = plan.smsRate()
                .multiply(BigDecimal.valueOf(billable))
                .setScale(CALCULATION_SCALE, MONEY_ROUNDING);
        return new RatedCharge(cost, 0, freeApplied);
    }
}
