package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.CdrEvent;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.CdrEventEntity;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Maps between {@link CdrEvent} and {@link CdrEventEntity}.
 */
@Component
public class CdrEventMapper {

    public CdrEventEntity toEntity(CdrEvent event, OffsetDateTime processingTimestamp) {
        return new CdrEventEntity(
                event.eventId(),
                event.customerId(),
                event.type(),
                event.quantity(),
                event.timestamp(),
                processingTimestamp
        );
    }
}
