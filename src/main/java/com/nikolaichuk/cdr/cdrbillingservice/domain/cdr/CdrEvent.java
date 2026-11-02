package com.nikolaichuk.cdr.cdrbillingservice.domain.cdr;

import com.nikolaichuk.cdr.cdrbillingservice.domain.shared.ValidationUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * CdrEvent value object: a single usage record to be rated.
 */
public record CdrEvent(UUID eventId, Long customerId, UsageType type, int quantity, OffsetDateTime timestamp) {

    public CdrEvent {
        ValidationUtils.requireNonNull(eventId, "eventId");
        ValidationUtils.requireNonNull(customerId, "customerId");
        ValidationUtils.requireNonNull(type, "type");
        ValidationUtils.requireNonNegative(quantity, "quantity");
        ValidationUtils.requireNonNull(timestamp, "timestamp");
    }
}
