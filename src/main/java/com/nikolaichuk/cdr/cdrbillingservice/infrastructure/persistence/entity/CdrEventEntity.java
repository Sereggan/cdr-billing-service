package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for CDR events.
 */
@Entity
@Table(name = "cdr_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CdrEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private UsageType type;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "event_timestamp", nullable = false)
    private OffsetDateTime eventTimestamp;

    @Column(name = "processed_at", nullable = false)
    private OffsetDateTime processedAt;

    public CdrEventEntity(UUID eventId, Long customerId, UsageType type, int quantity, OffsetDateTime eventTimestamp, OffsetDateTime processedAt) {
        this.eventId = eventId;
        this.customerId = customerId;
        this.type = type;
        this.quantity = quantity;
        this.eventTimestamp = eventTimestamp;
        this.processedAt = processedAt;
    }

}
