package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.adapter;

import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerAlreadyExistsException;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.CustomerEntity;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.SubscriptionPlanEntity;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.CustomerJpaRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.SubscriptionPlanJpaRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the {@link CustomerRepository}.
 */
@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;
    private final SubscriptionPlanJpaRepository subscriptionPlanJpaRepository;
    private final CustomerMapper mapper;

    @Override
    public Customer save(Customer customer) {
        SubscriptionPlanEntity plan = subscriptionPlanJpaRepository
                .findById(customer.getSubscriptionPlan().id())
                .orElseThrow(() -> new IllegalStateException(
                        "Subscription plan not found: " + customer.getSubscriptionPlan().id()));
        CustomerEntity entity = mapper.toEntity(customer, plan);
        try {
            return mapper.toDomain(customerJpaRepository.save(entity));
        } catch (DataIntegrityViolationException ex) {
            throw new CustomerAlreadyExistsException(customer.getEmail());
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerJpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Customer> findAllByIds(Collection<Long> ids) {
        return customerJpaRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Customer> findAll() {
        return customerJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
