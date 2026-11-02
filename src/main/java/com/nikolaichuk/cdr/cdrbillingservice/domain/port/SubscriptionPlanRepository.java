package com.nikolaichuk.cdr.cdrbillingservice.domain.port;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository {

    Optional<SubscriptionPlan> findByType(PlanType type);

    List<SubscriptionPlan> findAll();
}
