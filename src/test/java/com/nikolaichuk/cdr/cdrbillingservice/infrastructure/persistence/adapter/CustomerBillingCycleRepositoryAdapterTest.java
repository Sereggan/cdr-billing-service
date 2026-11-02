package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.adapter;

import com.nikolaichuk.cdr.cdrbillingservice.TestcontainersConfiguration;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.BillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.CustomerBillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.CustomerBillingCycleJpaRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.CustomerJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CustomerBillingCycleRepositoryAdapter}: idempotent row creation,
 * pessimistic lookup by customer and cycle, and in-place update of accumulated totals.
 *
 * <p>{@code @Transactional} is required because {@code findByCustomerAndCycle} acquires a
 * {@code PESSIMISTIC_WRITE} lock ({@code SELECT ... FOR UPDATE}), which mandates an active
 * transaction.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CustomerBillingCycleRepositoryAdapterTest {

    private static final BillingCycle CYCLE = new BillingCycle(YearMonth.of(2026, 3));
    private static final BillingCycle OTHER_CYCLE = new BillingCycle(YearMonth.of(2026, 4));
    private static final String EMAIL = "alice@example.com";
    private static final String NAME = "Alice";
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();

    @Autowired
    private CustomerBillingCycleRepositoryAdapter adapter;

    @Autowired
    private CustomerRepositoryAdapter customerAdapter;

    @Autowired
    private SubscriptionPlanRepositoryAdapter planAdapter;

    @Autowired
    private CustomerBillingCycleJpaRepository billingCycleJpaRepository;

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    @AfterEach
    void cleanUp() {
        billingCycleJpaRepository.deleteAll();
        customerJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("should create an empty billing cycle and report it as newly inserted")
    void shouldCreateEmptyBillingCycle() {
        Long customerId = savedCustomerId();

        boolean created = adapter.createIfAbsent(new CustomerBillingCycle(customerId, CYCLE));

        assertThat(created).isTrue();
        assertThat(adapter.findByCustomerAndCycle(customerId, CYCLE)).hasValueSatisfying(found -> {
            assertThat(found.getId()).isNotNull();
            assertThat(found.getCustomerId()).isEqualTo(customerId);
            assertThat(found.getCycle()).isEqualTo(CYCLE);
            assertThat(found.getTotalCost()).isEqualByComparingTo("0");
            assertThat(found.getUsedFreeSeconds()).isZero();
            assertThat(found.getUsedFreeSms()).isZero();
        });
    }

    @Test
    @DisplayName("should be idempotent: a second createIfAbsent reports no insert and keeps a single row")
    void shouldNotCreateDuplicateRow() {
        Long customerId = savedCustomerId();
        assertThat(adapter.createIfAbsent(new CustomerBillingCycle(customerId, CYCLE))).isTrue();

        boolean createdAgain = adapter.createIfAbsent(new CustomerBillingCycle(customerId, CYCLE));

        assertThat(createdAgain).isFalse();
        assertThat(billingCycleJpaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should update an existing row in place instead of inserting a duplicate")
    void shouldUpdateExistingRowInPlace() {
        Long customerId = savedCustomerId();
        adapter.createIfAbsent(new CustomerBillingCycle(customerId, CYCLE));
        CustomerBillingCycle loaded = adapter.findByCustomerAndCycle(customerId, CYCLE).orElseThrow();

        CustomerBillingCycle updated = new CustomerBillingCycle(
                loaded.getId(), customerId, CYCLE, new BigDecimal("3.50000000"), 1800, 7);
        adapter.save(updated);

        assertThat(billingCycleJpaRepository.count()).isEqualTo(1);
        assertThat(adapter.findByCustomerAndCycle(customerId, CYCLE)).hasValueSatisfying(found -> {
            assertThat(found.getId()).isEqualTo(loaded.getId());
            assertThat(found.getTotalCost()).isEqualByComparingTo("3.50000000");
            assertThat(found.getUsedFreeSeconds()).isEqualTo(1800);
            assertThat(found.getUsedFreeSms()).isEqualTo(7);
        });
    }

    @Test
    @DisplayName("should find a saved billing cycle by customer and cycle")
    void shouldFindByCustomerAndCycle() {
        Long customerId = savedCustomerId();
        adapter.createIfAbsent(new CustomerBillingCycle(customerId, CYCLE));

        Optional<CustomerBillingCycle> result = adapter.findByCustomerAndCycle(customerId, CYCLE);

        assertThat(result).hasValueSatisfying(found -> {
            assertThat(found.getCustomerId()).isEqualTo(customerId);
            assertThat(found.getCycle()).isEqualTo(CYCLE);
        });
    }

    @Test
    @DisplayName("should return empty when no billing cycle matches")
    void shouldReturnEmptyWhenNotFound() {
        Long customerId = savedCustomerId();
        adapter.createIfAbsent(new CustomerBillingCycle(customerId, CYCLE));

        Optional<CustomerBillingCycle> result = adapter.findByCustomerAndCycle(customerId, OTHER_CYCLE);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should rank the billing cycles of a cycle by total cost descending")
    void shouldRankByTotalCostDescending() {
        Long cheapest = savedBillingCycle("cheap@example.com", CYCLE, "1.00");
        Long priciest = savedBillingCycle("pricey@example.com", CYCLE, "30.00");
        Long middle = savedBillingCycle("middle@example.com", CYCLE, "20.00");

        List<CustomerBillingCycle> ranking = adapter.findTopByCycle(CYCLE, 10);

        assertThat(ranking)
                .extracting(CustomerBillingCycle::getCustomerId)
                .containsExactly(priciest, middle, cheapest);
    }

    @Test
    @DisplayName("should break ties by customer id so that the ranking stays stable")
    void shouldBreakTiesByCustomerId() {
        Long first = savedBillingCycle("first@example.com", CYCLE, "10.00");
        Long second = savedBillingCycle("second@example.com", CYCLE, "10.00");

        List<CustomerBillingCycle> ranking = adapter.findTopByCycle(CYCLE, 10);

        assertThat(ranking)
                .extracting(CustomerBillingCycle::getCustomerId)
                .containsExactly(Math.min(first, second), Math.max(first, second));
    }

    @Test
    @DisplayName("should return at most the requested number of entries")
    void shouldRespectTheLimit() {
        Long priciest = savedBillingCycle("pricey@example.com", CYCLE, "30.00");
        Long middle = savedBillingCycle("middle@example.com", CYCLE, "20.00");
        savedBillingCycle("cheap@example.com", CYCLE, "1.00");

        List<CustomerBillingCycle> ranking = adapter.findTopByCycle(CYCLE, 2);

        assertThat(ranking)
                .extracting(CustomerBillingCycle::getCustomerId)
                .containsExactly(priciest, middle);
    }

    @Test
    @DisplayName("should ignore billing cycles that belong to another cycle")
    void shouldIgnoreOtherCycles() {
        Long current = savedBillingCycle("current@example.com", CYCLE, "1.00");
        savedBillingCycle("other@example.com", OTHER_CYCLE, "99.00");

        List<CustomerBillingCycle> ranking = adapter.findTopByCycle(CYCLE, 10);

        assertThat(ranking)
                .extracting(CustomerBillingCycle::getCustomerId)
                .containsExactly(current);
    }

    private Long savedBillingCycle(String email, BillingCycle cycle, String totalCost) {
        Long customerId = savedCustomerId(email);
        adapter.createIfAbsent(new CustomerBillingCycle(customerId, cycle));
        CustomerBillingCycle created = adapter.findByCustomerAndCycle(customerId, cycle).orElseThrow();
        adapter.save(new CustomerBillingCycle(
                created.getId(), customerId, cycle, new BigDecimal(totalCost), 0, 0));
        return customerId;
    }

    private Long savedCustomerId() {
        return savedCustomerId(EMAIL);
    }

    private Long savedCustomerId(String email) {
        SubscriptionPlan plan = planAdapter.findByType(PlanType.STANDARD).orElseThrow();
        return customerAdapter.save(new Customer(email, NAME, plan, CREATED_AT)).getId();
    }
}
