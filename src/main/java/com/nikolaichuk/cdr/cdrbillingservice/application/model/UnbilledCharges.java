package com.nikolaichuk.cdr.cdrbillingservice.application.model;

import java.math.BigDecimal;

public record UnbilledCharges(Long customerId, BigDecimal totalCost, String billingCycle) {
}
