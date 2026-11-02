package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa;

import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.CdrEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CdrEventJpaRepository extends JpaRepository<CdrEventEntity, Long> {

    @Modifying
    @Query(
            value = """
                    INSERT INTO cdr_events (
                        event_id,
                        customer_id,
                        type,
                        quantity,
                        event_timestamp,
                        processed_at
                    )
                    VALUES (
                        :#{#event.eventId},
                        :#{#event.customerId},
                        :#{#event.type.name()},
                        :#{#event.quantity},
                        :#{#event.eventTimestamp},
                        :#{#event.processedAt}
                    )
                    ON CONFLICT (event_id) DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(@Param("event") CdrEventEntity event);
}


