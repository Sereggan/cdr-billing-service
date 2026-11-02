package com.nikolaichuk.cdr.cdrbillingservice.application.model;

public record FreeQuotas(Long customerId, int freeCallMinutes, int freeSms, String billingCycle) {
}
