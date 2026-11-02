package com.nikolaichuk.cdr.cdrbillingservice.domain.rating;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.shared.ValidationUtils;

/**
 * Input for a {@link RatingStrategy}.
 */
public record RatingContext(SubscriptionPlan plan, int quantity, int usedFreeSeconds, int usedFreeSms) {

    public RatingContext {
        ValidationUtils.requireNonNull(plan, "plan");
        ValidationUtils.requireNonNegative(quantity, "quantity");
        ValidationUtils.requireNonNegative(usedFreeSeconds, "usedFreeSeconds");
        ValidationUtils.requireNonNegative(usedFreeSms, "usedFreeSms");
    }
}
