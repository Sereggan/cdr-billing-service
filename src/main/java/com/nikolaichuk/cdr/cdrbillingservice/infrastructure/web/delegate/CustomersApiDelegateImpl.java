package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.delegate;

import com.nikolaichuk.cdr.cdrbillingservice.application.service.CustomerService;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.api.CustomersApiDelegate;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.CustomerResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.RegisterCustomerRequest;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper.CustomerWebMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Web adapter for the customers API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomersApiDelegateImpl implements CustomersApiDelegate {

    private final CustomerService customerService;
    private final CustomerWebMapper mapper;

    @Override
    public ResponseEntity<CustomerResponse> registerCustomer(RegisterCustomerRequest request) {
        log.info("POST /v1/customers email={} name={} plan={}",
                request.getEmail(), request.getName(), request.getPlan());
        Customer customer = customerService.saveCustomer(
                request.getEmail(),
                request.getName(),
                PlanType.valueOf(request.getPlan().name()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(customer));
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomer(Long customerId) {
        log.info("GET /v1/customers/{}", customerId);
        Customer customer = customerService.getCustomer(customerId);
        return ResponseEntity.ok(mapper.toResponse(customer));
    }
}
