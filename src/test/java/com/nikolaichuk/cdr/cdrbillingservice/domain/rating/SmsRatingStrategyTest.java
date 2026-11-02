package com.nikolaichuk.cdr.cdrbillingservice.domain.rating;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.SmsRatingStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SmsRatingStrategyTest {

    private static final BigDecimal SMS_RATE = new BigDecimal("0.01");
    private static final int PREMIUM_FREE_SMS = 20;

    private final SmsRatingStrategy strategy = new SmsRatingStrategy();

    @Test
    @DisplayName("supports the SMS usage type")
    void shouldSupportSmsType() {
        assertThat(strategy.supportedType()).isEqualTo(UsageType.SMS);
    }

    @Test
    @DisplayName("charges per message on a plan without free SMS")
    void shouldChargePerMessage() {
        RatedCharge charge = strategy.rate(new RatingContext(standardPlan(), 5, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("0.05");
        assertThat(charge.freeSmsUsed()).isZero();
    }

    @Test
    @DisplayName("applies free messages first while within quota")
    void shouldApplyFreeMessagesWithinQuota() {
        RatedCharge charge = strategy.rate(new RatingContext(premiumPlan(), 15, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("0.00");
        assertThat(charge.freeSmsUsed()).isEqualTo(15);
    }

    @Test
    @DisplayName("splits usage across the free quota boundary")
    void shouldSplitAcrossFreeQuotaBoundary() {
        RatedCharge charge = strategy.rate(new RatingContext(premiumPlan(), 10, 0, 15));

        assertThat(charge.freeSmsUsed()).isEqualTo(5);
        assertThat(charge.cost()).isEqualByComparingTo("0.05");
    }

    @Test
    @DisplayName("charges the full amount once the free quota is exhausted")
    void shouldChargeFullAmountWhenQuotaExhausted() {
        RatedCharge charge = strategy.rate(new RatingContext(premiumPlan(), 5, 0, PREMIUM_FREE_SMS));

        assertThat(charge.freeSmsUsed()).isZero();
        assertThat(charge.cost()).isEqualByComparingTo("0.05");
    }

    private SubscriptionPlan standardPlan() {
        return new SubscriptionPlan(1L, PlanType.STANDARD, 0, new BigDecimal("0.05"), 0,
                SMS_RATE, new BigDecimal("2.00"));
    }

    private SubscriptionPlan premiumPlan() {
        return new SubscriptionPlan(2L, PlanType.PREMIUM, 100, new BigDecimal("0.03"), PREMIUM_FREE_SMS,
                SMS_RATE, new BigDecimal("2.00"));
    }
}
