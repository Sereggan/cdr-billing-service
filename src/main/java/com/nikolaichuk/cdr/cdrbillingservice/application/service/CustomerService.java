package com.nikolaichuk.cdr.cdrbillingservice.application.service;

import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.application.exception.SubscriptionPlanNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerRepository;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Application service for customer use cases.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final Clock clock;

    @Transactional
    public Customer saveCustomer(String email, String name, PlanType planType) {
        log.info("Saving customer email={} name={} plan={}", email, name, planType);
        SubscriptionPlan plan = getSubscriptionPlan(planType);
        Customer customer = new Customer(email, name, plan, OffsetDateTime.now(clock));
        Customer saved = customerRepository.save(customer);
        log.info("Saved customer id={} email={} on plan={}", saved.getId(), saved.getEmail(), planType);
        return saved;
    }

    @Transactional(readOnly = true)
    public Customer getCustomer(Long customerId) {
        log.info("Fetching customer id={}", customerId);
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    private SubscriptionPlan getSubscriptionPlan(PlanType planType) {
        return subscriptionPlanRepository.findByType(planType)
                .orElseThrow(() -> new SubscriptionPlanNotFoundException(planType));
    }
}
