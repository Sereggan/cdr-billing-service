package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.CdrEvent;
import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.CdrEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CdrEventMapperTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final Long CUSTOMER_ID = 42L;
    private static final int QUANTITY = 120;
    private static final OffsetDateTime EVENT_TIMESTAMP =
            OffsetDateTime.of(2026, 3, 19, 10, 15, 30, 0, ZoneOffset.UTC);
    private static final OffsetDateTime PROCESSING_TIMESTAMP =
            OffsetDateTime.of(2026, 4, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private final CdrEventMapper mapper = new CdrEventMapper();

    @Test
    @DisplayName("toEntity() maps all fields and applies the processing timestamp")
    void shouldMapAllFieldsToEntity() {
        CdrEvent event = new CdrEvent(EVENT_ID, CUSTOMER_ID, UsageType.CALL, QUANTITY, EVENT_TIMESTAMP);

        CdrEventEntity entity = mapper.toEntity(event, PROCESSING_TIMESTAMP);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getEventId()).isEqualTo(EVENT_ID);
        assertThat(entity.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(entity.getType()).isEqualTo(UsageType.CALL);
        assertThat(entity.getQuantity()).isEqualTo(QUANTITY);
        assertThat(entity.getEventTimestamp()).isEqualTo(EVENT_TIMESTAMP);
        assertThat(entity.getProcessedAt()).isEqualTo(PROCESSING_TIMESTAMP);
    }
}
