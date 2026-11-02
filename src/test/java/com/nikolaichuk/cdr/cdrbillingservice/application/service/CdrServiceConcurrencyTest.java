package com.nikolaichuk.cdr.cdrbillingservice.application.service;

import com.nikolaichuk.cdr.cdrbillingservice.TestcontainersConfiguration;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.CdrProcessingResult;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.CdrProcessingStatus;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.BillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.CustomerBillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.CdrEvent;
import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CustomerBillingCycleRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.adapter.CustomerRepositoryAdapter;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.adapter.SubscriptionPlanRepositoryAdapter;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.CdrEventJpaRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.CustomerBillingCycleJpaRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.CustomerJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concurrency test for {@link CdrService}: many threads submit distinct CDR events for the
 * same customer and billing cycle simultaneously. Verifies that pessimistic locking
 * ({@code SELECT ... FOR UPDATE}) serialises writers so no update is lost, and that the
 * idempotent ensure-insert ({@code INSERT ... ON CONFLICT DO NOTHING}) never creates a
 * duplicate row.
 *
 * <p>The class is intentionally NOT {@code @Transactional}: each {@code process} call must run
 * in its own transaction so concurrent commits and locks behave as in production.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CdrServiceConcurrencyTest {

    private static final int THREADS = 20;
    private static final int CALL_SECONDS = 60;
    // STANDARD plan: free_call_minutes = 0, call_rate = 0.05 -> 0.05 * 60 / 60 = 0.05 per event
    private static final BigDecimal COST_PER_EVENT = new BigDecimal("0.05");
    private static final OffsetDateTime TIMESTAMP =
            OffsetDateTime.of(2026, 3, 19, 10, 15, 30, 0, ZoneOffset.UTC);
    private static final BillingCycle CYCLE = BillingCycle.from(TIMESTAMP);

    @Autowired
    private CdrService service;

    @Autowired
    private CustomerBillingCycleRepository billingCycleRepository;

    @Autowired
    private CustomerRepositoryAdapter customerAdapter;

    @Autowired
    private SubscriptionPlanRepositoryAdapter planAdapter;

    @Autowired
    private CustomerBillingCycleJpaRepository billingCycleJpaRepository;

    @Autowired
    private CdrEventJpaRepository cdrEventJpaRepository;

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    @AfterEach
    void cleanUp() {
        cdrEventJpaRepository.deleteAll();
        billingCycleJpaRepository.deleteAll();
        customerJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("should accumulate every concurrent charge without lost updates or duplicate rows")
    void shouldNotLoseUpdatesUnderConcurrency() throws Exception {
        Long customerId = savedCustomerId();
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger processed = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            List<Future<CdrProcessingResult>> futures = submitAll(pool, customerId, startGate, processed);
            startGate.countDown();
            for (Future<CdrProcessingResult> future : futures) {
                assertThat(future.get(30, TimeUnit.SECONDS).status()).isEqualTo(CdrProcessingStatus.PROCESSED);
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(processed).hasValue(THREADS);
        assertThat(billingCycleJpaRepository.count()).isEqualTo(1);
        CustomerBillingCycle result = billingCycleRepository.findByCustomerAndCycle(customerId, CYCLE).orElseThrow();
        assertThat(result.getTotalCost())
                .isEqualByComparingTo(COST_PER_EVENT.multiply(BigDecimal.valueOf(THREADS)));
    }

    @Test
    @DisplayName("should charge only once when the same event is processed twice (sequential idempotency)")
    void shouldBeIdempotentForRepeatedEvent() {
        Long customerId = savedCustomerId();
        CdrEvent event = callEvent(customerId);

        CdrProcessingResult first = service.process(event);
        CdrProcessingResult second = service.process(event);

        assertThat(first.status()).isEqualTo(CdrProcessingStatus.PROCESSED);
        assertThat(second.status()).isEqualTo(CdrProcessingStatus.DUPLICATE);
        assertThat(cdrEventJpaRepository.count()).isEqualTo(1);
        CustomerBillingCycle result = billingCycleRepository.findByCustomerAndCycle(customerId, CYCLE).orElseThrow();
        assertThat(result.getTotalCost()).isEqualByComparingTo(COST_PER_EVENT);
    }

    @Test
    @DisplayName("should charge only once when the same event races itself across threads")
    void shouldBeIdempotentUnderConcurrency() throws Exception {
        Long customerId = savedCustomerId();
        CdrEvent event = callEvent(customerId);
        CountDownLatch startGate = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            List<Future<CdrProcessingResult>> futures = java.util.stream.IntStream.range(0, THREADS)
                    .mapToObj(i -> pool.submit(() -> {
                        startGate.await();
                        return service.process(event);
                    }))
                    .toList();
            startGate.countDown();

            long processedCount = 0;
            long duplicateCount = 0;
            for (Future<CdrProcessingResult> future : futures) {
                CdrProcessingStatus status = future.get(30, TimeUnit.SECONDS).status();
                if (status == CdrProcessingStatus.PROCESSED) {
                    processedCount++;
                } else if (status == CdrProcessingStatus.DUPLICATE) {
                    duplicateCount++;
                }
            }
            assertThat(processedCount).isEqualTo(1);
            assertThat(duplicateCount).isEqualTo(THREADS - 1L);
        } finally {
            pool.shutdownNow();
        }

        assertThat(cdrEventJpaRepository.count()).isEqualTo(1);
        CustomerBillingCycle result = billingCycleRepository.findByCustomerAndCycle(customerId, CYCLE).orElseThrow();
        assertThat(result.getTotalCost()).isEqualByComparingTo(COST_PER_EVENT);
    }

    private List<Future<CdrProcessingResult>> submitAll(ExecutorService pool, Long customerId,
                                                        CountDownLatch startGate, AtomicInteger processed) {
        return java.util.stream.IntStream.range(0, THREADS)
                .mapToObj(i -> pool.submit(() -> {
                    startGate.await();
                    CdrProcessingResult result = service.process(callEvent(customerId));
                    processed.incrementAndGet();
                    return result;
                }))
                .toList();
    }

    private CdrEvent callEvent(Long customerId) {
        return new CdrEvent(UUID.randomUUID(), customerId, UsageType.CALL, CALL_SECONDS, TIMESTAMP);
    }

    private Long savedCustomerId() {
        SubscriptionPlan plan = planAdapter.findByType(PlanType.STANDARD).orElseThrow();
        return customerAdapter.save(
                new Customer("alice@example.com", "Alice", plan, OffsetDateTime.now(ZoneOffset.UTC))).getId();
    }
}
