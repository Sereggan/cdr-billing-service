package com.nikolaichuk.cdr.cdrbillingservice.application.model;

import java.util.List;

public record TopCustomers(String billingCycle, List<TopCustomer> customers) {
}
