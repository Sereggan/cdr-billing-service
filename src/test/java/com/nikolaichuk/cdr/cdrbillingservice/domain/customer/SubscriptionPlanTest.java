package com.nikolaichuk.cdr.cdrbillingservice.domain.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionPlanTest {

    private static final BigDecimal CALL_RATE = new BigDecimal("0.05");
    private static final BigDecimal SMS_RATE = new BigDecimal("0.01");
    private static final BigDecimal DATA_RATE = new BigDecimal("2.00");

    @Test
    @DisplayName("constructor accepts a valid plan configuration")
    void shouldCreatePlanWithValidConfiguration() {
        SubscriptionPlan plan = new SubscriptionPlan(1L, PlanType.PREMIUM, 100, new BigDecimal("0.03"),
                20, SMS_RATE, DATA_RATE);

        assertThat(plan.type()).isEqualTo(PlanType.PREMIUM);
        assertThat(plan.freeCallMinutes()).isEqualTo(100);
        assertThat(plan.callRate()).isEqualByComparingTo("0.03");
    }

    @Test
    @DisplayName("constructor throws when the plan type is null")
    void shouldThrowWhenTypeIsNull() {
        assertThatThrownBy(() -> new SubscriptionPlan(1L, null, 0, CALL_RATE, 0, SMS_RATE, DATA_RATE))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("type");
    }

    @Test
    @DisplayName("constructor throws when free call minutes are negative")
    void shouldThrowWhenFreeCallMinutesAreNegative() {
        assertThatThrownBy(() -> createPlanWith(-1, CALL_RATE, 0, SMS_RATE, DATA_RATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("freeCallMinutes");
    }

    @Test
    @DisplayName("constructor throws when the call rate is negative")
    void shouldThrowWhenCallRateIsNegative() {
        assertThatThrownBy(() -> createPlanWith(0, new BigDecimal("-0.01"), 0, SMS_RATE, DATA_RATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("callRate");
    }

    @Test
    @DisplayName("constructor throws when free SMS are negative")
    void shouldThrowWhenFreeSmsAreNegative() {
        assertThatThrownBy(() -> createPlanWith(0, CALL_RATE, -1, SMS_RATE, DATA_RATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("freeSms");
    }

    @Test
    @DisplayName("constructor throws when the SMS rate is negative")
    void shouldThrowWhenSmsRateIsNegative() {
        assertThatThrownBy(() -> createPlanWith(0, CALL_RATE, 0, new BigDecimal("-0.01"), DATA_RATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("smsRate");
    }

    @Test
    @DisplayName("constructor throws when the data rate is negative")
    void shouldThrowWhenDataRateIsNegative() {
        assertThatThrownBy(() -> createPlanWith(0, CALL_RATE, 0, SMS_RATE, new BigDecimal("-2.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dataRate");
    }

    @Test
    @DisplayName("constructor throws when the call rate is null")
    void shouldThrowWhenCallRateIsNull() {
        assertThatThrownBy(() -> createPlanWith(0, null, 0, SMS_RATE, DATA_RATE))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("callRate");
    }

    private SubscriptionPlan createPlanWith(int freeCallMinutes, BigDecimal callRate, int freeSms,
                                            BigDecimal smsRate, BigDecimal dataRate) {
        return new SubscriptionPlan(1L, PlanType.STANDARD, freeCallMinutes, callRate, freeSms, smsRate, dataRate);
    }
}
