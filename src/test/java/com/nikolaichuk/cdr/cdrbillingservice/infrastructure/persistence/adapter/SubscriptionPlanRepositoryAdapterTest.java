package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.adapter;

import com.nikolaichuk.cdr.cdrbillingservice.TestcontainersConfiguration;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SubscriptionPlanRepositoryAdapter} against the Flyway-seeded plans.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SubscriptionPlanRepositoryAdapterTest {

    private static final Long STANDARD_ID = 1L;
    private static final Long PREMIUM_ID = 2L;
    private static final int PREMIUM_FREE_CALL_MINUTES = 100;
    private static final int PREMIUM_FREE_SMS = 20;
    private static final BigDecimal STANDARD_CALL_RATE = new BigDecimal("0.0500");
    private static final BigDecimal PREMIUM_CALL_RATE = new BigDecimal("0.0300");
    private static final BigDecimal SMS_RATE = new BigDecimal("0.0100");
    private static final BigDecimal DATA_RATE = new BigDecimal("2.0000");

    @Autowired
    private SubscriptionPlanRepositoryAdapter adapter;

    @Test
    @DisplayName("should find the seeded STANDARD plan by type")
    void shouldFindStandardPlanByType() {
        Optional<SubscriptionPlan> result = adapter.findByType(PlanType.STANDARD);

        assertThat(result).hasValueSatisfying(plan -> {
            assertThat(plan.id()).isEqualTo(STANDARD_ID);
            assertThat(plan.type()).isEqualTo(PlanType.STANDARD);
            assertThat(plan.freeCallMinutes()).isZero();
            assertThat(plan.callRate()).isEqualByComparingTo(STANDARD_CALL_RATE);
            assertThat(plan.freeSms()).isZero();
            assertThat(plan.smsRate()).isEqualByComparingTo(SMS_RATE);
            assertThat(plan.dataRate()).isEqualByComparingTo(DATA_RATE);
        });
    }

    @Test
    @DisplayName("should find the seeded PREMIUM plan by type")
    void shouldFindPremiumPlanByType() {
        Optional<SubscriptionPlan> result = adapter.findByType(PlanType.PREMIUM);

        assertThat(result).hasValueSatisfying(plan -> {
            assertThat(plan.id()).isEqualTo(PREMIUM_ID);
            assertThat(plan.type()).isEqualTo(PlanType.PREMIUM);
            assertThat(plan.freeCallMinutes()).isEqualTo(PREMIUM_FREE_CALL_MINUTES);
            assertThat(plan.callRate()).isEqualByComparingTo(PREMIUM_CALL_RATE);
            assertThat(plan.freeSms()).isEqualTo(PREMIUM_FREE_SMS);
            assertThat(plan.smsRate()).isEqualByComparingTo(SMS_RATE);
            assertThat(plan.dataRate()).isEqualByComparingTo(DATA_RATE);
        });
    }
}
