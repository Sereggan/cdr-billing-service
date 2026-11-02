package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.adapter;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.CdrEvent;
import com.nikolaichuk.cdr.cdrbillingservice.domain.port.CdrEventRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.CdrEventJpaRepository;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper.CdrEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Persistence adapter for the {@link CdrEventRepository}.
 */
@Component
@RequiredArgsConstructor
public class CdrEventRepositoryAdapter implements CdrEventRepository {

    private final Clock clock;
    private final CdrEventMapper mapper;
    private final CdrEventJpaRepository repository;

    @Override
    public boolean createIfAbsent(CdrEvent event) {
        int result = repository.insertIfAbsent(mapper.toEntity(event, OffsetDateTime.now(clock)));
        return result == 1;
    }
}
