package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.CustomerEntity;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.SubscriptionPlanEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link Customer} and {@link CustomerEntity}.
 */
@Component
@RequiredArgsConstructor
public class CustomerMapper {

    private final SubscriptionPlanMapper subscriptionPlanMapper;

    public Customer toDomain(CustomerEntity entity) {
        return new Customer(
                entity.getId(),
                entity.getEmail(),
                entity.getName(),
                subscriptionPlanMapper.toDomain(entity.getSubscriptionPlan()),
                entity.getCreatedAt()
        );
    }

    public CustomerEntity toEntity(Customer customer, SubscriptionPlanEntity subscriptionPlan) {
        return new CustomerEntity(
                customer.getEmail(),
                customer.getName(),
                subscriptionPlan,
                customer.getCreatedAt()
        );
    }
}
