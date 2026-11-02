package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.adapter;

import com.nikolaichuk.cdr.cdrbillingservice.TestcontainersConfiguration;
import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.CdrEvent;
import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.CdrEventJpaRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.CustomerJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CdrEventRepositoryAdapter}: persistence of new events and
 * idempotent handling of duplicate {@code eventId}s.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CdrEventRepositoryAdapterTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final int QUANTITY = 120;
    private static final OffsetDateTime EVENT_TIMESTAMP =
            OffsetDateTime.of(2026, 3, 19, 10, 15, 30, 0, ZoneOffset.UTC);
    private static final String EMAIL = "alice@example.com";
    private static final String NAME = "Alice";
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();

    @Autowired
    private CdrEventRepositoryAdapter adapter;

    @Autowired
    private CustomerRepositoryAdapter customerAdapter;

    @Autowired
    private SubscriptionPlanRepositoryAdapter planAdapter;

    @Autowired
    private CdrEventJpaRepository cdrEventJpaRepository;

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    @AfterEach
    void cleanUp() {
        cdrEventJpaRepository.deleteAll();
        customerJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("should persist a new event and report it as stored")
    void shouldPersistNewEvent() {
        Long customerId = savedCustomerId();

        boolean stored = adapter.createIfAbsent(event(customerId));

        assertThat(stored).isTrue();
        assertThat(cdrEventJpaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should report a duplicate event as not stored without side effects")
    void shouldIgnoreDuplicateEvent() {
        Long customerId = savedCustomerId();
        adapter.createIfAbsent(event(customerId));

        boolean storedAgain = adapter.createIfAbsent(event(customerId));

        assertThat(storedAgain).isFalse();
        assertThat(cdrEventJpaRepository.count()).isEqualTo(1);
    }

    private CdrEvent event(Long customerId) {
        return new CdrEvent(EVENT_ID, customerId, UsageType.CALL, QUANTITY, EVENT_TIMESTAMP);
    }

    private Long savedCustomerId() {
        SubscriptionPlan plan = planAdapter.findByType(PlanType.STANDARD).orElseThrow();
        return customerAdapter.save(new Customer(EMAIL, NAME, plan, CREATED_AT)).getId();
    }
}
