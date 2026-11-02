package com.nikolaichuk.cdr.cdrbillingservice.application.service;

import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.FreeQuotas;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.TopCustomer;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.TopCustomers;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.UnbilledCharges;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.BillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.CustomerBillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerBillingCycleRepository;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    private static final Instant NOW = Instant.parse("2026-03-15T10:15:30Z");
    private static final YearMonth CYCLE_VALUE = YearMonth.of(2026, 3);
    private static final BillingCycle CYCLE = new BillingCycle(CYCLE_VALUE);
    private static final String CYCLE_LABEL = "2026-03";

    private static final Long CUSTOMER_ID = 42L;
    private static final int TOP_CUSTOMERS_LIMIT = 10;

    private static final int PREMIUM_FREE_MINUTES = 100;
    private static final int PREMIUM_FREE_SMS = 20;
    private static final int SECONDS_PER_MINUTE = 60;

    private static final BigDecimal CALL_RATE = new BigDecimal("0.03");
    private static final BigDecimal SMS_RATE = new BigDecimal("0.01");
    private static final BigDecimal DATA_RATE = new BigDecimal("2.00");

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerBillingCycleRepository customerBillingCycleRepository;

    private ReportingService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new ReportingService(customerRepository, customerBillingCycleRepository, clock);
    }

    @Nested
    @DisplayName("getUnbilledCharges")
    class GetUnbilledCharges {

        @Test
        @DisplayName("should return the accumulated cost of the current cycle")
        void shouldReturnAccumulatedCost() {
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(premiumCustomer()));
            when(customerBillingCycleRepository.findByCustomerAndCycle(CUSTOMER_ID, CYCLE))
                    .thenReturn(Optional.of(billingCycle(new BigDecimal("12.34"), 0, 0)));

            UnbilledCharges result = service.getUnbilledCharges(CUSTOMER_ID);

            assertThat(result.customerId()).isEqualTo(CUSTOMER_ID);
            assertThat(result.totalCost()).isEqualByComparingTo("12.34");
            assertThat(result.billingCycle()).isEqualTo(CYCLE_LABEL);
        }

        @Test
        @DisplayName("should return zero when the customer has no traffic in the current cycle")
        void shouldReturnZeroWhenNoTraffic() {
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(premiumCustomer()));
            when(customerBillingCycleRepository.findByCustomerAndCycle(CUSTOMER_ID, CYCLE))
                    .thenReturn(Optional.empty());

            UnbilledCharges result = service.getUnbilledCharges(CUSTOMER_ID);

            assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.billingCycle()).isEqualTo(CYCLE_LABEL);
        }

        @Test
        @DisplayName("should throw when the customer does not exist")
        void shouldThrowWhenCustomerNotFound() {
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getUnbilledCharges(CUSTOMER_ID))
                    .isInstanceOf(CustomerNotFoundException.class);
            verify(customerBillingCycleRepository, never()).findByCustomerAndCycle(any(), any());
        }
    }

    @Nested
    @DisplayName("getFreeQuotas")
    class GetFreeQuotas {

        @Test
        @DisplayName("should return the full plan quota when nothing has been used yet")
        void shouldReturnFullQuotaWhenUnused() {
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(premiumCustomer()));
            when(customerBillingCycleRepository.findByCustomerAndCycle(CUSTOMER_ID, CYCLE))
                    .thenReturn(Optional.empty());

            FreeQuotas result = service.getFreeQuotas(CUSTOMER_ID);

            assertThat(result.customerId()).isEqualTo(CUSTOMER_ID);
            assertThat(result.freeCallMinutes()).isEqualTo(PREMIUM_FREE_MINUTES);
            assertThat(result.freeSms()).isEqualTo(PREMIUM_FREE_SMS);
            assertThat(result.billingCycle()).isEqualTo(CYCLE_LABEL);
        }

        @Test
        @DisplayName("should subtract the used quota")
        void shouldSubtractUsedQuota() {
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(premiumCustomer()));
            when(customerBillingCycleRepository.findByCustomerAndCycle(CUSTOMER_ID, CYCLE))
                    .thenReturn(Optional.of(billingCycle(BigDecimal.ZERO, 10 * SECONDS_PER_MINUTE, 5)));

            FreeQuotas result = service.getFreeQuotas(CUSTOMER_ID);

            assertThat(result.freeCallMinutes()).isEqualTo(PREMIUM_FREE_MINUTES - 10);
            assertThat(result.freeSms()).isEqualTo(PREMIUM_FREE_SMS - 5);
        }

        @Test
        @DisplayName("should floor a partially used minute instead of rounding it up")
        void shouldFloorPartiallyUsedMinute() {
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(premiumCustomer()));
            when(customerBillingCycleRepository.findByCustomerAndCycle(CUSTOMER_ID, CYCLE))
                    .thenReturn(Optional.of(billingCycle(BigDecimal.ZERO, 90, 0)));

            FreeQuotas result = service.getFreeQuotas(CUSTOMER_ID);

            // 6000s - 90s = 5910s = 98.5 min -> floored to 98
            assertThat(result.freeCallMinutes()).isEqualTo(98);
        }

        @Test
        @DisplayName("should never report a negative quota when it is fully used up")
        void shouldClampExhaustedQuotaToZero() {
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(premiumCustomer()));
            when(customerBillingCycleRepository.findByCustomerAndCycle(CUSTOMER_ID, CYCLE))
                    .thenReturn(Optional.of(billingCycle(
                            BigDecimal.ZERO,
                            PREMIUM_FREE_MINUTES * SECONDS_PER_MINUTE,
                            PREMIUM_FREE_SMS)));

            FreeQuotas result = service.getFreeQuotas(CUSTOMER_ID);

            assertThat(result.freeCallMinutes()).isZero();
            assertThat(result.freeSms()).isZero();
        }

        @Test
        @DisplayName("should report zero quotas for a plan without free units")
        void shouldReportZeroForPlanWithoutFreeUnits() {
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(standardCustomer()));
            when(customerBillingCycleRepository.findByCustomerAndCycle(CUSTOMER_ID, CYCLE))
                    .thenReturn(Optional.empty());

            FreeQuotas result = service.getFreeQuotas(CUSTOMER_ID);

            assertThat(result.freeCallMinutes()).isZero();
            assertThat(result.freeSms()).isZero();
        }

        @Test
        @DisplayName("should throw when the customer does not exist")
        void shouldThrowWhenCustomerNotFound() {
            when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getFreeQuotas(CUSTOMER_ID))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getTopCustomers")
    class GetTopCustomers {

        @Test
        @DisplayName("should keep the ranking order of the repository and enrich it with customer details")
        void shouldKeepRankingOrderAndEnrich() {
            when(customerBillingCycleRepository.findTopByCycle(CYCLE, TOP_CUSTOMERS_LIMIT))
                    .thenReturn(List.of(
                            billingCycle(1L, new BigDecimal("30.00")),
                            billingCycle(2L, new BigDecimal("20.00")),
                            billingCycle(3L, new BigDecimal("10.00"))));
            // the repository returns the customers in an arbitrary order
            when(customerRepository.findAllByIds(List.of(1L, 2L, 3L)))
                    .thenReturn(List.of(
                            customer(3L, "carol@example.com", "Carol"),
                            customer(1L, "alice@example.com", "Alice"),
                            customer(2L, "bob@example.com", "Bob")));

            TopCustomers result = service.getTopCustomers();

            assertThat(result.billingCycle()).isEqualTo(CYCLE_LABEL);
            assertThat(result.customers())
                    .extracting(TopCustomer::customerId, TopCustomer::name, TopCustomer::email)
                    .containsExactly(
                            tuple(1L, "Alice", "alice@example.com"),
                            tuple(2L, "Bob", "bob@example.com"),
                            tuple(3L, "Carol", "carol@example.com"));
            assertThat(result.customers().getFirst().totalCost()).isEqualByComparingTo("30.00");
        }

        @Test
        @DisplayName("should return an empty ranking when nobody had traffic in the cycle")
        void shouldReturnEmptyRanking() {
            when(customerBillingCycleRepository.findTopByCycle(CYCLE, TOP_CUSTOMERS_LIMIT))
                    .thenReturn(List.of());
            when(customerRepository.findAllByIds(List.of())).thenReturn(List.of());

            TopCustomers result = service.getTopCustomers();

            assertThat(result.billingCycle()).isEqualTo(CYCLE_LABEL);
            assertThat(result.customers()).isEmpty();
        }

        @Test
        @DisplayName("should load the customers of the whole ranking in a single query")
        void shouldLoadCustomersInSingleQuery() {
            when(customerBillingCycleRepository.findTopByCycle(CYCLE, TOP_CUSTOMERS_LIMIT))
                    .thenReturn(List.of(
                            billingCycle(1L, new BigDecimal("30.00")),
                            billingCycle(2L, new BigDecimal("20.00"))));
            when(customerRepository.findAllByIds(List.of(1L, 2L)))
                    .thenReturn(List.of(
                            customer(1L, "alice@example.com", "Alice"),
                            customer(2L, "bob@example.com", "Bob")));

            service.getTopCustomers();

            verify(customerRepository).findAllByIds(List.of(1L, 2L));
            verify(customerRepository, never()).findById(any());
        }
    }

    private static CustomerBillingCycle billingCycle(BigDecimal totalCost, int usedFreeSeconds, int usedFreeSms) {
        return new CustomerBillingCycle(1L, CUSTOMER_ID, CYCLE, totalCost, usedFreeSeconds, usedFreeSms);
    }

    private static CustomerBillingCycle billingCycle(Long customerId, BigDecimal totalCost) {
        return new CustomerBillingCycle(customerId, customerId, CYCLE, totalCost, 0, 0);
    }

    private static Customer customer(Long id, String email, String name) {
        return new Customer(id, email, name, premiumPlan(), OffsetDateTime.now(ZoneOffset.UTC));
    }

    private static Customer premiumCustomer() {
        return new Customer(CUSTOMER_ID, "alice@example.com", "Alice", premiumPlan(),
                OffsetDateTime.now(ZoneOffset.UTC));
    }

    private static Customer standardCustomer() {
        return new Customer(CUSTOMER_ID, "bob@example.com", "Bob", standardPlan(),
                OffsetDateTime.now(ZoneOffset.UTC));
    }

    private static SubscriptionPlan premiumPlan() {
        return new SubscriptionPlan(2L, PlanType.PREMIUM, PREMIUM_FREE_MINUTES, CALL_RATE,
                PREMIUM_FREE_SMS, SMS_RATE, DATA_RATE);
    }

    private static SubscriptionPlan standardPlan() {
        return new SubscriptionPlan(1L, PlanType.STANDARD, 0, new BigDecimal("0.05"), 0, SMS_RATE, DATA_RATE);
    }
}
