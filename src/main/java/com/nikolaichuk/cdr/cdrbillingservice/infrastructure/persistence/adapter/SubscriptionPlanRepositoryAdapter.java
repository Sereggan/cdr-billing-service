package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.adapter;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.SubscriptionPlanRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.SubscriptionPlanJpaRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper.SubscriptionPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the {@link SubscriptionPlanRepository}.
 */
@Component
@RequiredArgsConstructor
public class SubscriptionPlanRepositoryAdapter implements SubscriptionPlanRepository {

    private final SubscriptionPlanJpaRepository jpaRepository;
    private final SubscriptionPlanMapper mapper;

    @Override
    public Optional<SubscriptionPlan> findByType(PlanType type) {
        return jpaRepository.findByType(type).map(mapper::toDomain);
    }

    @Override
    public List<SubscriptionPlan> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
