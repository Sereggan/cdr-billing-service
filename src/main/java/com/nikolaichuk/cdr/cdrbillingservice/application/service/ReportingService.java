package com.nikolaichuk.cdr.cdrbillingservice.application.service;

import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.FreeQuotas;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.TopCustomer;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.TopCustomers;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.UnbilledCharges;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.BillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.CustomerBillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerBillingCycleRepository;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application service for reporting use cases.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportingService {

    private static final int TOP_CUSTOMERS_LIMIT = 10;

    private final CustomerRepository customerRepository;
    private final CustomerBillingCycleRepository customerBillingCycleRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public UnbilledCharges getUnbilledCharges(Long customerId) {
        var cycle = BillingCycle.from(OffsetDateTime.now(clock));
        log.info("Reading unbilled charges for customer {} in cycle {}", customerId, cycle.value());

        getCustomer(customerId);
        var totalCost = getTotalCost(customerId, cycle);

        log.info("Unbilled charges for customer {} in cycle {}: {}", customerId, cycle.value(), totalCost);
        return new UnbilledCharges(customerId, totalCost, cycle.value().toString());
    }

    @Transactional(readOnly = true)
    public FreeQuotas getFreeQuotas(Long customerId) {
        var cycle = BillingCycle.from(OffsetDateTime.now(clock));
        log.info("Reading free quotas for customer {} in cycle {}", customerId, cycle.value());

        SubscriptionPlan plan = getCustomer(customerId).getSubscriptionPlan();
        var billingCycle = customerBillingCycleRepository.findByCustomerAndCycle(customerId, cycle);
        int usedFreeSeconds = billingCycle.map(CustomerBillingCycle::getUsedFreeSeconds).orElse(0);
        int usedFreeSms = billingCycle.map(CustomerBillingCycle::getUsedFreeSms).orElse(0);

        // The quota is tracked in seconds, so the remainder is floored to whole minutes.
        int freeCallMinutes = plan.remainingFreeMinutes(usedFreeSeconds);
        int freeSms = plan.remainingFreeSms(usedFreeSms);

        log.info("Free quotas for customer {} in cycle {}: {} minutes, {} SMS",
                customerId, cycle.value(), freeCallMinutes, freeSms);
        return new FreeQuotas(customerId, freeCallMinutes, freeSms, cycle.value().toString());
    }

    @Transactional(readOnly = true)
    public TopCustomers getTopCustomers() {
        var cycle = BillingCycle.from(OffsetDateTime.now(clock));
        log.info("Reading top {} customers for cycle {}", TOP_CUSTOMERS_LIMIT, cycle.value());

        List<CustomerBillingCycle> ranking =
                customerBillingCycleRepository.findTopByCycle(cycle, TOP_CUSTOMERS_LIMIT);
        Map<Long, Customer> customersById = getCustomersById(ranking);

        List<TopCustomer> customers = ranking.stream()
                .map(billingCycle -> toTopCustomer(billingCycle, customersById.get(billingCycle.getCustomerId())))
                .toList();

        log.info("Top customers for cycle {}: {} entries", cycle.value(), customers.size());
        return new TopCustomers(cycle.value().toString(), customers);
    }

    /**
     * Loads the ranked customers in a single query to keep the report free of N+1 lookups.
     */
    private Map<Long, Customer> getCustomersById(List<CustomerBillingCycle> ranking) {
        List<Long> customerIds = ranking.stream()
                .map(CustomerBillingCycle::getCustomerId)
                .toList();

        return customerRepository.findAllByIds(customerIds).stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity()));
    }

    private TopCustomer toTopCustomer(CustomerBillingCycle billingCycle, Customer customer) {
        return new TopCustomer(customer.getId(), customer.getName(), customer.getEmail(), billingCycle.getTotalCost());
    }

    private BigDecimal getTotalCost(Long customerId, BillingCycle cycle) {
        return customerBillingCycleRepository.findByCustomerAndCycle(customerId, cycle)
                .map(CustomerBillingCycle::getTotalCost)
                .orElse(BigDecimal.ZERO);
    }

    private Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Customer not found while reading reports: {}", customerId);
                    return new CustomerNotFoundException(customerId);
                });
    }
}
