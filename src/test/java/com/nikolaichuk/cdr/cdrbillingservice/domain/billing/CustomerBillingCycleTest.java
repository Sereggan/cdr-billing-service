package com.nikolaichuk.cdr.cdrbillingservice.domain.billing;

import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatedCharge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerBillingCycleTest {

    private static final Long CUSTOMER_ID = 1L;
    private static final BillingCycle CYCLE = new BillingCycle(YearMonth.of(2026, 3));

    @Test
    @DisplayName("starts empty with zeroed totals")
    void shouldStartEmpty() {
        CustomerBillingCycle billing = new CustomerBillingCycle(CUSTOMER_ID, CYCLE);

        assertThat(billing.getTotalCost()).isEqualByComparingTo("0.00");
        assertThat(billing.getUsedFreeSeconds()).isZero();
        assertThat(billing.getUsedFreeSms()).isZero();
    }

    @Test
    @DisplayName("accumulates cost from an applied charge")
    void shouldAccumulateCost() {
        CustomerBillingCycle billing = new CustomerBillingCycle(CUSTOMER_ID, CYCLE);

        billing.apply(new RatedCharge(new BigDecimal("1.50"), 0, 0));
        billing.apply(new RatedCharge(new BigDecimal("0.75"), 0, 0));

        assertThat(billing.getTotalCost()).isEqualByComparingTo("2.25");
    }

    @Test
    @DisplayName("accumulates consumed free quota from applied charges")
    void shouldAccumulateFreeQuota() {
        CustomerBillingCycle billing = new CustomerBillingCycle(CUSTOMER_ID, CYCLE);

        billing.apply(new RatedCharge(BigDecimal.ZERO, 10, 5));
        billing.apply(new RatedCharge(BigDecimal.ZERO, 3, 2));

        assertThat(billing.getUsedFreeSeconds()).isEqualTo(13);
        assertThat(billing.getUsedFreeSms()).isEqualTo(7);
    }

    @Test
    @DisplayName("throws when applying a null charge")
    void shouldThrowWhenChargeIsNull() {
        CustomerBillingCycle billing = new CustomerBillingCycle(CUSTOMER_ID, CYCLE);

        assertThatThrownBy(() -> billing.apply(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("charge");
    }

    @Test
    @DisplayName("throws when reconstituted with a null customer id")
    void shouldThrowWhenCustomerIdIsNull() {
        assertThatThrownBy(() -> new CustomerBillingCycle(10L, null, CYCLE, BigDecimal.ZERO, 0, 0))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("customerId");
    }

    @Test
    @DisplayName("throws when reconstituted with a negative total cost")
    void shouldThrowWhenTotalCostIsNegative() {
        assertThatThrownBy(() -> new CustomerBillingCycle(10L, CUSTOMER_ID, CYCLE, new BigDecimal("-0.01"), 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalCost");
    }
}
