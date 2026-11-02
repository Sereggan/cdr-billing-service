package com.nikolaichuk.cdr.cdrbillingservice.domain.billing;

import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatedCharge;
import com.nikolaichuk.cdr.cdrbillingservice.domain.shared.ValidationUtils;

import java.math.BigDecimal;

/**
 * CustomerBillingCycle aggregate root: accumulated cost and consumed free quota
 * for one customer within one billing cycle.
 */
public class CustomerBillingCycle {

    private final Long id;
    private final Long customerId;
    private final BillingCycle cycle;
    private BigDecimal totalCost;
    private int usedFreeSeconds;
    private int usedFreeSms;

    /**
     * Reconstitutes an existing aggregate.
     */
    public CustomerBillingCycle(Long id, Long customerId, BillingCycle cycle, BigDecimal totalCost,
                                int usedFreeSeconds, int usedFreeSms) {
        this.id = id;
        this.customerId = ValidationUtils.requireNonNull(customerId, "customerId");
        this.cycle = ValidationUtils.requireNonNull(cycle, "cycle");
        this.totalCost = ValidationUtils.requireNonNegative(totalCost, "totalCost");
        this.usedFreeSeconds = ValidationUtils.requireNonNegative(usedFreeSeconds, "usedFreeSeconds");
        this.usedFreeSms = ValidationUtils.requireNonNegative(usedFreeSms, "usedFreeSms");
    }

    /**
     * Starts a new empty aggregate for a customer and cycle.
     */
    public CustomerBillingCycle(Long customerId, BillingCycle cycle) {
        this(null, customerId, cycle, BigDecimal.ZERO, 0, 0);
    }

    /**
     * Accumulates a rated charge into the running totals.
     */
    public void apply(RatedCharge charge) {
        ValidationUtils.requireNonNull(charge, "charge");
        this.totalCost = this.totalCost.add(charge.cost());
        this.usedFreeSeconds += charge.freeSecondsUsed();
        this.usedFreeSms += charge.freeSmsUsed();
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public BillingCycle getCycle() {
        return cycle;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public int getUsedFreeSeconds() {
        return usedFreeSeconds;
    }

    public int getUsedFreeSms() {
        return usedFreeSms;
    }
}
