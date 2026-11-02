package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.adapter;

import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.BillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.CustomerBillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerBillingCycleRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.CustomerBillingCycleJpaRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper.CustomerBillingCycleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the {@link CustomerBillingCycleRepository}.
 */
@Component
@RequiredArgsConstructor
public class CustomerBillingCycleRepositoryAdapter implements CustomerBillingCycleRepository {

    private final CustomerBillingCycleJpaRepository jpaRepository;
    private final CustomerBillingCycleMapper mapper;

    @Override
    public Optional<CustomerBillingCycle> findByCustomerAndCycle(Long customerId, BillingCycle cycle) {
        return jpaRepository.findByCustomerIdAndCycle(customerId, cycle.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<CustomerBillingCycle> findByCustomerAndCycleForUpdate(Long customerId, BillingCycle cycle) {
        return jpaRepository.findForUpdate(customerId, cycle.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<CustomerBillingCycle> findTopByCycle(BillingCycle cycle, int limit) {
        return jpaRepository
                .findByCycleOrderByTotalCostDescCustomerIdAsc(cycle.value(), PageRequest.of(0, limit)).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public CustomerBillingCycle save(CustomerBillingCycle billingCycle) {
        var saved = jpaRepository.save(mapper.toEntity(billingCycle));

        return mapper.toDomain(saved);
    }

    @Override
    public boolean createIfAbsent(CustomerBillingCycle billingCycle) {
        int inserted = jpaRepository.insertIfAbsent(billingCycle.getCustomerId(), billingCycle.getCycle().value().toString());
        return inserted == 1;
    }
}
