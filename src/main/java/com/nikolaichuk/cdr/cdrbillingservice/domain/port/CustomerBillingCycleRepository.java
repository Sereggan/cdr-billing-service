package com.nikolaichuk.cdr.cdrbillingservice.domain.port;

import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.BillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.CustomerBillingCycle;

import java.util.List;
import java.util.Optional;

public interface CustomerBillingCycleRepository {

    Optional<CustomerBillingCycle> findByCustomerAndCycle(Long customerId, BillingCycle cycle);

    Optional<CustomerBillingCycle> findByCustomerAndCycleForUpdate(Long customerId, BillingCycle cycle);

    /**
     * Returns at most {@code limit} billing cycles of the given cycle, ordered by total cost
     * descending. Ties are broken by customer id to keep the ranking stable.
     */
    List<CustomerBillingCycle> findTopByCycle(BillingCycle cycle, int limit);

    CustomerBillingCycle save(CustomerBillingCycle billing);

    /**
     * Records the customer billing cycle if its id is new. Returns {@code false} if the cbc
     * already exists (duplicate).
     */
    boolean createIfAbsent(CustomerBillingCycle billing);
}
