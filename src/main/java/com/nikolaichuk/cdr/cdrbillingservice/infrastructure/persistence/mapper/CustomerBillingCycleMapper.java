package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper;

import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.BillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.CustomerBillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.CustomerBillingCycleEntity;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link CustomerBillingCycle} and {@link CustomerBillingCycleEntity}.
 */
@Component
public class CustomerBillingCycleMapper {

    public CustomerBillingCycle toDomain(CustomerBillingCycleEntity entity) {
        return new CustomerBillingCycle(
                entity.getId(),
                entity.getCustomerId(),
                new BillingCycle(entity.getCycle()),
                entity.getTotalCost(),
                entity.getUsedFreeSeconds(),
                entity.getUsedFreeSms()
        );
    }

    public CustomerBillingCycleEntity toEntity(CustomerBillingCycle billingCycle) {
        return new CustomerBillingCycleEntity(
                billingCycle.getId(),
                billingCycle.getCustomerId(),
                billingCycle.getCycle().value(),
                billingCycle.getTotalCost(),
                billingCycle.getUsedFreeSeconds(),
                billingCycle.getUsedFreeSms()
        );
    }
}
