package com.nikolaichuk.cdr.cdrbillingservice.domain.port;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.CdrEvent;

public interface CdrEventRepository {

    /**
     * Records the event if its id is new. Returns {@code false} if the event
     * already exists (duplicate), enforcing idempotency atomically.
     */
    boolean createIfAbsent(CdrEvent event);
}
