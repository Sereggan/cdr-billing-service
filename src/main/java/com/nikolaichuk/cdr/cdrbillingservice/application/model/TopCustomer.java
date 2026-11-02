package com.nikolaichuk.cdr.cdrbillingservice.application.model;

import java.math.BigDecimal;

public record TopCustomer(Long customerId, String name, String email, BigDecimal totalCost) {
}
