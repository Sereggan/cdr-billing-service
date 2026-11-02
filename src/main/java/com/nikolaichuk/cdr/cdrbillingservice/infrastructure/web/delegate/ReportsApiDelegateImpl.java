package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.delegate;

import com.nikolaichuk.cdr.cdrbillingservice.application.service.ReportingService;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.api.ReportsApiDelegate;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.FreeQuotasResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.TopCustomersResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.UnbilledChargesResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper.ReportWebMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Web adapter for the reporting API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportsApiDelegateImpl implements ReportsApiDelegate {

    private final ReportingService reportingService;
    private final ReportWebMapper mapper;

    @Override
    public ResponseEntity<UnbilledChargesResponse> getUnbilledCharges(Long customerId) {
        log.info("GET /v1/customers/{}/unbilled-charges", customerId);
        return ResponseEntity.ok(mapper.toResponse(reportingService.getUnbilledCharges(customerId)));
    }

    @Override
    public ResponseEntity<FreeQuotasResponse> getFreeQuotas(Long customerId) {
        log.info("GET /v1/customers/{}/free-quotas", customerId);
        return ResponseEntity.ok(mapper.toResponse(reportingService.getFreeQuotas(customerId)));
    }

    @Override
    public ResponseEntity<TopCustomersResponse> getTopCustomers() {
        log.info("GET /v1/reports/top-customers");
        return ResponseEntity.ok(mapper.toResponse(reportingService.getTopCustomers()));
    }
}
