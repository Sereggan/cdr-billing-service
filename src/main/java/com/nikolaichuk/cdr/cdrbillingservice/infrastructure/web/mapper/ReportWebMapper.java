package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper;

import com.nikolaichuk.cdr.cdrbillingservice.application.model.FreeQuotas;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.TopCustomer;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.TopCustomers;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.UnbilledCharges;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.FreeQuotasResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.TopCustomerResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.TopCustomersResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.UnbilledChargesResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Maps reporting results to web response DTOs.
 *
 * <p>Costs are accumulated at a higher scale internally and are rounded to currency precision
 * only here, at the API boundary, so that no rounding error is ever fed back into the billing
 * cycle.
 */
@Component
public class ReportWebMapper {

    private static final int CURRENCY_SCALE = 2;

    public UnbilledChargesResponse toResponse(UnbilledCharges unbilledCharges) {
        return new UnbilledChargesResponse(
                unbilledCharges.customerId(),
                toCurrency(unbilledCharges.totalCost()),
                unbilledCharges.billingCycle());
    }

    public FreeQuotasResponse toResponse(FreeQuotas freeQuotas) {
        return new FreeQuotasResponse(
                freeQuotas.customerId(),
                freeQuotas.freeCallMinutes(),
                freeQuotas.freeSms(),
                freeQuotas.billingCycle());
    }

    public TopCustomersResponse toResponse(TopCustomers topCustomers) {
        return new TopCustomersResponse(
                topCustomers.billingCycle(),
                topCustomers.customers().stream()
                        .map(this::toResponse)
                        .toList());
    }

    private TopCustomerResponse toResponse(TopCustomer topCustomer) {
        TopCustomerResponse response =
                new TopCustomerResponse(topCustomer.customerId(), toCurrency(topCustomer.totalCost()));
        response.setName(topCustomer.name());
        response.setEmail(topCustomer.email());
        return response;
    }

    private BigDecimal toCurrency(BigDecimal amount) {
        return amount.setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }
}
