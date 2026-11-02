package com.nikolaichuk.cdr.cdrbillingservice.domain.rating;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.CallRatingStrategy;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.DataRatingStrategy;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.SmsRatingStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RatingEngineTest {

    private final RatingEngine engine = new RatingEngine(
            List.of(new CallRatingStrategy(), new SmsRatingStrategy(), new DataRatingStrategy()));

    @Test
    @DisplayName("dispatches CALL usage to the call strategy")
    void shouldDispatchCall() {
        RatedCharge charge = engine.rate(UsageType.CALL, new RatingContext(plan(), 60, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("0.05");
    }

    @Test
    @DisplayName("dispatches SMS usage to the SMS strategy")
    void shouldDispatchSms() {
        RatedCharge charge = engine.rate(UsageType.SMS, new RatingContext(plan(), 3, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("0.03");
    }

    @Test
    @DisplayName("dispatches DATA usage to the data strategy")
    void shouldDispatchData() {
        RatedCharge charge = engine.rate(UsageType.DATA, new RatingContext(plan(), 1024, 0, 0));

        assertThat(charge.cost()).isEqualByComparingTo("2.00");
    }

    @Test
    @DisplayName("throws when no strategy is registered for a type")
    void shouldThrowWhenNoStrategyRegistered() {
        RatingEngine emptyEngine = new RatingEngine(List.of());

        assertThatThrownBy(() -> emptyEngine.rate(UsageType.CALL, new RatingContext(plan(), 60, 0, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CALL");
    }

    private SubscriptionPlan plan() {
        return new SubscriptionPlan(1L, PlanType.STANDARD, 0, new BigDecimal("0.05"), 0,
                new BigDecimal("0.01"), new BigDecimal("2.00"));
    }
}
