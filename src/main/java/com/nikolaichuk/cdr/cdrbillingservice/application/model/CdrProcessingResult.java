package com.nikolaichuk.cdr.cdrbillingservice.application.model;

import java.util.UUID;

public record CdrProcessingResult(UUID eventId, CdrProcessingStatus status) {
}
