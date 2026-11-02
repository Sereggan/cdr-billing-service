package com.nikolaichuk.cdr.cdrbillingservice.application.service;

import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.CdrProcessingResult;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.CdrProcessingStatus;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.BillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.CustomerBillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.CdrEvent;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CdrEventRepository;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerBillingCycleRepository;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerRepository;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatedCharge;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatingContext;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for CDR.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CdrService {

    private final CdrEventRepository cdrEventRepository;
    private final CustomerRepository customerRepository;
    private final CustomerBillingCycleRepository customerBillingCycleRepository;

    private final RatingEngine ratingEngine;

    /**
     * Processes a CDR event: saves it, rates it, and updates the customer's billing cycle.
     */
    @Transactional
    public CdrProcessingResult process(CdrEvent cdrEvent) {
        log.info("Saving CDR event: {}", cdrEvent);

        Customer customer = customerRepository.findById(cdrEvent.customerId())
                .orElseThrow(() -> {
                    log.error("Customer not found for CDR event: {}", cdrEvent);
                    return new CustomerNotFoundException(cdrEvent.customerId());
                });

        // Save the CDR event, if it already exists, return DUPLICATE
        boolean isNewEvent = cdrEventRepository.createIfAbsent(cdrEvent);
        if (!isNewEvent) {
            log.info("Received duplicate CDR event, skipping: {}", cdrEvent);
            return new CdrProcessingResult(cdrEvent.eventId(), CdrProcessingStatus.DUPLICATE);
        }

        // Ensure the billing cycle exists and lock it for update
        CustomerBillingCycle billingCycle = lockBillingCycle(cdrEvent);

        // Rate the CDR event
        RatedCharge ratedCharge = getRatedCharge(cdrEvent, customer, billingCycle);

        // Update the billing cycle with the rated charge
        billingCycle.apply(ratedCharge);
        customerBillingCycleRepository.save(billingCycle);
        log.info("Processed CDR event: {}, updated billing cycle: {}", cdrEvent, billingCycle);

        return new CdrProcessingResult(cdrEvent.eventId(), CdrProcessingStatus.PROCESSED);
    }

    private CustomerBillingCycle lockBillingCycle(CdrEvent cdrEvent) {
        BillingCycle cycle = BillingCycle.from(cdrEvent.timestamp());
        customerBillingCycleRepository.createIfAbsent(new CustomerBillingCycle(cdrEvent.customerId(), cycle));

        return customerBillingCycleRepository.findByCustomerAndCycleForUpdate(cdrEvent.customerId(), cycle)
                .orElseThrow(() -> new IllegalStateException(
                        "Billing cycle vanished after ensure-insert for customer %d cycle %s"
                                .formatted(cdrEvent.customerId(), cycle.value())));
    }

    private RatedCharge getRatedCharge(CdrEvent cdrEvent, Customer customer, CustomerBillingCycle billingCycle) {
        return ratingEngine.rate(cdrEvent.type(), new RatingContext(customer.getSubscriptionPlan(),
                cdrEvent.quantity(), billingCycle.getUsedFreeSeconds(), billingCycle.getUsedFreeSms()));
    }
}
