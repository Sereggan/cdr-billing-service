package com.nikolaichuk.cdr.cdrbillingservice.domain.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();
    private static final String EMAIL = "alice@example.com";
    private static final String NAME = "Alice";
    private static final BigDecimal SMS_RATE = new BigDecimal("0.01");
    private static final BigDecimal DATA_RATE = new BigDecimal("2.00");
    private static final BigDecimal STANDARD_CALL_RATE = new BigDecimal("0.05");
    private static final BigDecimal PREMIUM_CALL_RATE = new BigDecimal("0.03");

    @Test
    @DisplayName("constructor creates a customer without an assigned identity")
    void shouldRegisterCustomerWithoutIdentity() {
        Customer customer = new Customer(EMAIL, NAME, createStandardPlan(), CREATED_AT);

        assertThat(customer.getId()).isNull();
        assertThat(customer.getName()).isEqualTo(NAME);
        assertThat(customer.getSubscriptionPlan()).isEqualTo(createStandardPlan());
        assertThat(customer.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    @DisplayName("constructor reconstitutes a customer with an existing identity")
    void shouldReconstituteCustomerWithIdentity() {
        Customer customer = new Customer(42L, EMAIL, NAME, createStandardPlan(), CREATED_AT);

        assertThat(customer.getId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("constructor normalizes the email to trimmed lowercase")
    void shouldNormalizeEmailToTrimmedLowercase() {
        Customer customer = new Customer("  Alice@Example.COM  ", NAME, createStandardPlan(), CREATED_AT);

        assertThat(customer.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("constructor throws when the email is blank")
    void shouldThrowWhenEmailIsBlank() {
        assertThatThrownBy(() -> new Customer("   ", NAME, createStandardPlan(), CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("constructor throws when the name is blank")
    void shouldThrowWhenNameIsBlank() {
        assertThatThrownBy(() -> new Customer(EMAIL, " ", createStandardPlan(), CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    @DisplayName("constructor throws when the subscription plan is null")
    void shouldThrowWhenPlanIsNull() {
        assertThatThrownBy(() -> new Customer(EMAIL, NAME, null, CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("subscriptionPlan");
    }

    @Test
    @DisplayName("constructor throws when createdAt is null")
    void shouldThrowWhenCreatedAtIsNull() {
        assertThatThrownBy(() -> new Customer(EMAIL, NAME, createStandardPlan(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("createdAt");
    }

    @Test
    @DisplayName("equality is based on the normalized email, ignoring case")
    void shouldBeEqualWhenEmailMatchesIgnoringCase() {
        Customer a = new Customer(1L, "Alice@Example.com", NAME, createStandardPlan(), CREATED_AT);
        Customer b = new Customer(2L, EMAIL, "Alicia", createPremiumPlan(), CREATED_AT);

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    @DisplayName("customers with different emails are not equal")
    void shouldNotBeEqualWhenEmailDiffers() {
        Customer a = new Customer(EMAIL, NAME, createStandardPlan(), CREATED_AT);
        Customer b = new Customer("bob@example.com", "Bob", createStandardPlan(), CREATED_AT);

        assertThat(a).isNotEqualTo(b);
    }

    private SubscriptionPlan createStandardPlan() {
        return new SubscriptionPlan(1L, PlanType.STANDARD, 0, STANDARD_CALL_RATE, 0, SMS_RATE, DATA_RATE);
    }

    private SubscriptionPlan createPremiumPlan() {
        return new SubscriptionPlan(2L, PlanType.PREMIUM, 100, PREMIUM_CALL_RATE, 20, SMS_RATE, DATA_RATE);
    }
}
