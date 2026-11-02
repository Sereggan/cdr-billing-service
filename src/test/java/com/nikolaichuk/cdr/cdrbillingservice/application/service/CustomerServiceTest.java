package com.nikolaichuk.cdr.cdrbillingservice.application.service;

import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.application.exception.SubscriptionPlanNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerRepository;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.SubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final Long CUSTOMER_ID = 42L;
    private static final String EMAIL = "alice@example.com";
    private static final String NAME = "Alice";
    private static final Instant NOW = Instant.parse("2026-03-19T10:15:30Z");
    private static final BigDecimal STANDARD_CALL_RATE = new BigDecimal("0.0500");
    private static final BigDecimal SMS_RATE = new BigDecimal("0.0100");
    private static final BigDecimal DATA_RATE = new BigDecimal("2.0000");

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService(customerRepository, subscriptionPlanRepository, clock);
    }

    @Nested
    @DisplayName("saveCustomer")
    class SaveCustomer {

        @Test
        @DisplayName("should register a customer on the requested plan with the clock timestamp")
        void shouldRegisterCustomerOnRequestedPlan() {
            when(subscriptionPlanRepository.findByType(PlanType.STANDARD)).thenReturn(Optional.of(standardPlan()));
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Customer result = service.saveCustomer(EMAIL, NAME, PlanType.STANDARD);

            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).save(captor.capture());
            Customer saved = captor.getValue();
            assertThat(saved.getEmail()).isEqualTo(EMAIL);
            assertThat(saved.getName()).isEqualTo(NAME);
            assertThat(saved.getSubscriptionPlan().type()).isEqualTo(PlanType.STANDARD);
            assertThat(saved.getCreatedAt().toInstant()).isEqualTo(NOW);
            assertThat(result).isSameAs(saved);
        }

        @Test
        @DisplayName("should throw when the plan does not exist on registration")
        void shouldThrowWhenPlanNotFoundOnRegister() {
            when(subscriptionPlanRepository.findByType(PlanType.PREMIUM)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.saveCustomer(EMAIL, NAME, PlanType.PREMIUM))
                    .isInstanceOf(SubscriptionPlanNotFoundException.class);
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCustomer")
    class GetCustomer {

        @Test
        @DisplayName("should return the customer by id")
        void shouldReturnCustomerById() {
            Customer customer = customer();
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

            Customer result = service.getCustomer(CUSTOMER_ID);

            assertThat(result).isSameAs(customer);
        }

        @Test
        @DisplayName("should throw when the customer does not exist")
        void shouldThrowWhenCustomerNotFound() {
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getCustomer(CUSTOMER_ID))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    private Customer customer() {
        return new Customer(CUSTOMER_ID, EMAIL, NAME, standardPlan(), OffsetDateTime.now(clock));
    }

    private SubscriptionPlan standardPlan() {
        return new SubscriptionPlan(1L, PlanType.STANDARD, 0, STANDARD_CALL_RATE, 0, SMS_RATE, DATA_RATE);
    }
}
