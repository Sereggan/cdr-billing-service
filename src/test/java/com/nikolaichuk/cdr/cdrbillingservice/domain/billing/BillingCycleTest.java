package com.nikolaichuk.cdr.cdrbillingservice.domain.billing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BillingCycleTest {

    @Test
    @DisplayName("derives the cycle from a UTC timestamp")
    void shouldDeriveCycleFromUtcTimestamp() {
        BillingCycle cycle = BillingCycle.from(OffsetDateTime.parse("2026-03-19T10:15:30Z"));

        assertThat(cycle.value()).isEqualTo(YearMonth.of(2026, 3));
    }

    @Test
    @DisplayName("assigns a late-arriving event to the month of its event timestamp")
    void shouldAssignBackdatedEventToEventMonth() {
        BillingCycle cycle = BillingCycle.from(OffsetDateTime.parse("2026-03-31T23:30:00Z"));

        assertThat(cycle.value()).isEqualTo(YearMonth.of(2026, 3));
    }

    @Test
    @DisplayName("normalizes a non-UTC offset before deriving the cycle")
    void shouldNormalizeOffsetToUtc() {
        BillingCycle cycle = BillingCycle.from(OffsetDateTime.of(2026, 4, 1, 1, 30, 0, 0, ZoneOffset.ofHours(2)));

        assertThat(cycle.value()).isEqualTo(YearMonth.of(2026, 3));
    }

    @Test
    @DisplayName("throws when the timestamp is null")
    void shouldThrowWhenTimestampIsNull() {
        assertThatThrownBy(() -> BillingCycle.from(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("timestamp");
    }
}
