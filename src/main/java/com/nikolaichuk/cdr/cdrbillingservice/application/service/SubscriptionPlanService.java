package com.nikolaichuk.cdr.cdrbillingservice.application.service;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for subscription plans.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getPlans() {
        log.info("Listing all subscription plans");
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();
        log.info("Found {} subscription plans", plans.size());
        return plans;
    }
}
