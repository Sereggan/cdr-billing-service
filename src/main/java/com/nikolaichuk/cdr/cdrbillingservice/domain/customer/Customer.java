package com.nikolaichuk.cdr.cdrbillingservice.domain.customer;

import com.nikolaichuk.cdr.cdrbillingservice.domain.shared.ValidationUtils;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

/**
 * Customer aggregate root.
 */
public class Customer {

    private final Long id;
    private final String email;
    private final String name;
    private final SubscriptionPlan subscriptionPlan;
    private final OffsetDateTime createdAt;

    /**
     * Reconstitutes an existing aggregate.
     */
    public Customer(Long id, String email, String name, SubscriptionPlan subscriptionPlan, OffsetDateTime createdAt) {
        this.id = id;
        this.email = normalizeEmail(email);
        this.name = ValidationUtils.requireNotBlank(name, "name");
        this.subscriptionPlan = ValidationUtils.requireNonNull(subscriptionPlan, "subscriptionPlan");
        this.createdAt = ValidationUtils.requireNonNull(createdAt, "createdAt");
    }

    /**
     * Creates a new aggregate without an assigned identity.
     */
    public Customer(String email, String name, SubscriptionPlan subscriptionPlan, OffsetDateTime createdAt) {
        this(null, email, name, subscriptionPlan, createdAt);
    }

    private static String normalizeEmail(String email) {
        return ValidationUtils.requireNotBlank(email, "email").trim().toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Customer that)) {
            return false;
        }
        return email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
