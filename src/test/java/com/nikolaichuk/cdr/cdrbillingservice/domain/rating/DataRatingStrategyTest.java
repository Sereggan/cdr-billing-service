package com.nikolaichuk.cdr.cdrbillingservice.domain.rating;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.DataRatingStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DataRatingStrategyTest {

    private static final BigDecimal DATA_RATE = new BigDecimal("2.00");

    private final DataRatingStrategy strategy = new DataRatingStrategy();

    @Test
    @DisplayName("supports the DATA usage type")
    void shouldSupportDataType() {
        assertThat(strategy.supportedType()).isEqualTo(UsageType.DATA);
    }

    @Test
    @DisplayName("charges a full gigabyte at the data rate")
    void shouldChargeFullGigabyte() {
        RatedCharge charge = strategy.rate(new RatingContext(plan(), 1024, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("2.00");
    }

    @Test
    @DisplayName("charges proportionally for a partial gigabyte")
    void shouldChargeProportionallyForPartialGigabyte() {
        RatedCharge charge = strategy.rate(new RatingContext(plan(), 512, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("1.00");
    }

    @Test
    @DisplayName("charges zero for zero megabytes")
    void shouldChargeZeroForZeroMegabytes() {
        RatedCharge charge = strategy.rate(new RatingContext(plan(), 0, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("never consumes free quota")
    void shouldNotConsumeFreeQuota() {
        RatedCharge charge = strategy.rate(new RatingContext(plan(), 2048, 0, 0));

        assertThat(charge.freeSecondsUsed()).isZero();
        assertThat(charge.freeSmsUsed()).isZero();
    }

    private SubscriptionPlan plan() {
        return new SubscriptionPlan(1L, PlanType.STANDARD, 0, new BigDecimal("0.05"), 0,
                new BigDecimal("0.01"), DATA_RATE);
    }
}
