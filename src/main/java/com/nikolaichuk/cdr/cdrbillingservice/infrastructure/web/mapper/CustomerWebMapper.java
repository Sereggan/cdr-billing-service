package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.CustomerResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.PlanResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.PlanType;
import org.springframework.stereotype.Component;

/**
 * Maps domain models to web response DTOs.
 */
@Component
public class CustomerWebMapper {

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getEmail(),
                customer.getName(),
                toResponse(customer.getSubscriptionPlan()),
                customer.getCreatedAt());
    }

    public PlanResponse toResponse(SubscriptionPlan plan) {
        return new PlanResponse(
                plan.id(),
                PlanType.valueOf(plan.type().name()),
                plan.freeCallMinutes(),
                plan.callRate(),
                plan.freeSms(),
                plan.smsRate(),
                plan.dataRate());
    }
}
