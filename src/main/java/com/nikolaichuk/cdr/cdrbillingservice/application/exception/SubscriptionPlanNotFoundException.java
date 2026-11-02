package com.nikolaichuk.cdr.cdrbillingservice.application.exception;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;

/**
 * Thrown when a subscription plan cannot be found for a given type.
 */
public class SubscriptionPlanNotFoundException extends RuntimeException {

    public SubscriptionPlanNotFoundException(PlanType type) {
        super("Subscription plan not found: " + type);
    }
}
