package com.nikolaichuk.cdr.cdrbillingservice.domain.rating;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.CallRatingStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CallRatingStrategyTest {

    private static final BigDecimal STANDARD_CALL_RATE = new BigDecimal("0.05");
    private static final BigDecimal PREMIUM_CALL_RATE = new BigDecimal("0.03");
    private static final int PREMIUM_FREE_MINUTES = 100;
    private static final int SECONDS_PER_MINUTE = 60;

    private final CallRatingStrategy strategy = new CallRatingStrategy();

    @Test
    @DisplayName("supports the CALL usage type")
    void shouldSupportCallType() {
        assertThat(strategy.supportedType()).isEqualTo(UsageType.CALL);
    }

    @Test
    @DisplayName("charges proportionally per second without rounding up")
    void shouldChargeProportionallyBySeconds() {
        RatedCharge charge = strategy.rate(new RatingContext(standardPlan(), 61, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("0.05083333");
        assertThat(charge.freeSecondsUsed()).isZero();
    }

    @Test
    @DisplayName("charges an exact minute without rounding up")
    void shouldChargeExactMinute() {
        RatedCharge charge = strategy.rate(new RatingContext(standardPlan(), 120, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("0.10");
    }

    @Test
    @DisplayName("charges zero for zero seconds")
    void shouldChargeZeroForZeroSeconds() {
        RatedCharge charge = strategy.rate(new RatingContext(standardPlan(), 0, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("0.00");
        assertThat(charge.freeSecondsUsed()).isZero();
    }

    @Test
    @DisplayName("applies free seconds first and charges nothing while within quota")
    void shouldApplyFreeSecondsWithinQuota() {
        RatedCharge charge = strategy.rate(new RatingContext(premiumPlan(), 600, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("0.00");
        assertThat(charge.freeSecondsUsed()).isEqualTo(600);
    }

    @Test
    @DisplayName("splits usage across the free quota boundary")
    void shouldSplitAcrossFreeQuotaBoundary() {
        RatedCharge charge = strategy.rate(
                new RatingContext(premiumPlan(), 6000, 95 * SECONDS_PER_MINUTE, 0));

        assertThat(charge.freeSecondsUsed()).isEqualTo(300);
        assertThat(charge.cost()).isEqualByComparingTo("2.85");
    }

    @Test
    @DisplayName("charges the full amount once the free quota is exhausted")
    void shouldChargeFullAmountWhenQuotaExhausted() {
        RatedCharge charge = strategy.rate(
                new RatingContext(premiumPlan(), 600, PREMIUM_FREE_MINUTES * SECONDS_PER_MINUTE, 0));

        assertThat(charge.freeSecondsUsed()).isZero();
        assertThat(charge.cost()).isEqualByComparingTo("0.30");
    }

    private SubscriptionPlan standardPlan() {
        return new SubscriptionPlan(1L, PlanType.STANDARD, 0, STANDARD_CALL_RATE, 0,
                new BigDecimal("0.01"), new BigDecimal("2.00"));
    }

    private SubscriptionPlan premiumPlan() {
        return new SubscriptionPlan(2L, PlanType.PREMIUM, PREMIUM_FREE_MINUTES, PREMIUM_CALL_RATE, 20,
                new BigDecimal("0.01"), new BigDecimal("2.00"));
    }
}
