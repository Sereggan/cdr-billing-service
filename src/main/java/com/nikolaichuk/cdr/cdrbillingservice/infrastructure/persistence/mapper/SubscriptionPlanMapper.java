package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.SubscriptionPlanEntity;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link SubscriptionPlan} and {@link SubscriptionPlanEntity}.
 */
@Component
public class SubscriptionPlanMapper {

    public SubscriptionPlan toDomain(SubscriptionPlanEntity entity) {
        return new SubscriptionPlan(
                entity.getId(),
                entity.getType(),
                entity.getFreeCallMinutes(),
                entity.getCallRate(),
                entity.getFreeSms(),
                entity.getSmsRate(),
                entity.getDataRate()
        );
    }
}
