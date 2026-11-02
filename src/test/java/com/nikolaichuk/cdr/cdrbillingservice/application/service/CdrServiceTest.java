package com.nikolaichuk.cdr.cdrbillingservice.application.service;

import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.CdrProcessingResult;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.CdrProcessingStatus;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.BillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.CustomerBillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.CdrEvent;
import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CdrEventRepository;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerBillingCycleRepository;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerRepository;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatingEngine;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.CallRatingStrategy;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.DataRatingStrategy;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.SmsRatingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CdrServiceTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final Long CUSTOMER_ID = 42L;
    private static final OffsetDateTime TIMESTAMP = OffsetDateTime.now(ZoneOffset.UTC);
    private static final BigDecimal STANDARD_CALL_RATE = new BigDecimal("0.05");
    private static final BigDecimal SMS_RATE = new BigDecimal("0.01");
    private static final BigDecimal DATA_RATE = new BigDecimal("2.00");

    @Mock
    private CdrEventRepository cdrEventRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerBillingCycleRepository customerBillingCycleRepository;

    private CdrService service;

    @BeforeEach
    void setUp() {
        RatingEngine ratingEngine = new RatingEngine(
                List.of(new CallRatingStrategy(), new SmsRatingStrategy(), new DataRatingStrategy()));
        service = new CdrService(cdrEventRepository, customerRepository, customerBillingCycleRepository, ratingEngine);
    }

    @Test
    @DisplayName("should acknowledge a duplicate event without rating or touching the billing cycle")
    void shouldAcknowledgeDuplicate() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(standardCustomer()));
        when(cdrEventRepository.createIfAbsent(any(CdrEvent.class))).thenReturn(false);

        CdrProcessingResult result = service.process(callEvent(60));

        assertThat(result.eventId()).isEqualTo(EVENT_ID);
        assertThat(result.status()).isEqualTo(CdrProcessingStatus.DUPLICATE);
        verify(customerBillingCycleRepository, never()).createIfAbsent(any());
        verify(customerBillingCycleRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw without storing the event when the customer does not exist")
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.process(callEvent(60)))
                .isInstanceOf(CustomerNotFoundException.class);
        verify(cdrEventRepository, never()).createIfAbsent(any());
        verify(customerBillingCycleRepository, never()).createIfAbsent(any());
        verify(customerBillingCycleRepository, never()).save(any());
    }

    @Test
    @DisplayName("should rate a new event into a freshly started billing cycle")
    void shouldRateNewEventIntoNewCycle() {
        when(cdrEventRepository.createIfAbsent(any(CdrEvent.class))).thenReturn(true);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(standardCustomer()));
        // createIfAbsent has ensured an empty row exists; the locking read returns it
        when(customerBillingCycleRepository.findByCustomerAndCycleForUpdate(eq(CUSTOMER_ID), any(BillingCycle.class)))
                .thenReturn(Optional.of(new CustomerBillingCycle(CUSTOMER_ID, BillingCycle.from(TIMESTAMP))));

        CdrProcessingResult result = service.process(callEvent(60));

        assertThat(result.status()).isEqualTo(CdrProcessingStatus.PROCESSED);
        CustomerBillingCycle saved = captureSavedBillingCycle();
        assertThat(saved.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(saved.getCycle().value()).isEqualTo(YearMonth.from(TIMESTAMP));
        // STANDARD: 0.05/min, 60s billable -> 0.05 * 60 / 60
        assertThat(saved.getTotalCost()).isEqualByComparingTo("0.05");
    }

    @Test
    @DisplayName("should accumulate a rated charge onto an existing billing cycle")
    void shouldAccumulateOntoExistingCycle() {
        when(cdrEventRepository.createIfAbsent(any(CdrEvent.class))).thenReturn(true);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(standardCustomer()));
        CustomerBillingCycle existing = new CustomerBillingCycle(
                7L, CUSTOMER_ID, BillingCycle.from(TIMESTAMP), new BigDecimal("1.00"), 0, 0);
        when(customerBillingCycleRepository.findByCustomerAndCycleForUpdate(eq(CUSTOMER_ID), any(BillingCycle.class)))
                .thenReturn(Optional.of(existing));

        CdrProcessingResult result = service.process(callEvent(60));

        assertThat(result.status()).isEqualTo(CdrProcessingStatus.PROCESSED);
        CustomerBillingCycle saved = captureSavedBillingCycle();
        assertThat(saved.getTotalCost()).isEqualByComparingTo("1.05");
    }

    private CustomerBillingCycle captureSavedBillingCycle() {
        ArgumentCaptor<CustomerBillingCycle> captor = ArgumentCaptor.forClass(CustomerBillingCycle.class);
        verify(customerBillingCycleRepository).save(captor.capture());
        return captor.getValue();
    }

    private CdrEvent callEvent(int seconds) {
        return new CdrEvent(EVENT_ID, CUSTOMER_ID, UsageType.CALL, seconds, TIMESTAMP);
    }

    private Customer standardCustomer() {
        return new Customer(CUSTOMER_ID, "alice@example.com", "Alice", standardPlan(),
                OffsetDateTime.now(ZoneOffset.UTC));
    }

    private SubscriptionPlan standardPlan() {
        return new SubscriptionPlan(1L, PlanType.STANDARD, 0, STANDARD_CALL_RATE, 0, SMS_RATE, DATA_RATE);
    }
}
