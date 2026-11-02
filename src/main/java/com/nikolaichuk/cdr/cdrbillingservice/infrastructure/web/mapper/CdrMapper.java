package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper;

import com.nikolaichuk.cdr.cdrbillingservice.application.model.CdrProcessingResult;
import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.CdrEvent;
import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.CdrProcessingStatus;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.CdrRequest;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.CdrResponse;
import org.springframework.stereotype.Component;

/**
 * Maps between CDR web DTOs and application/domain models.
 */
@Component
public class CdrMapper {

    public CdrEvent toDomain(CdrRequest request) {
        return new CdrEvent(
                request.getEventId(),
                request.getCustomerId(),
                UsageType.valueOf(request.getType().name()),
                request.getQuantity(),
                request.getTimestamp());
    }

    public CdrResponse toResponse(CdrProcessingResult result) {
        return new CdrResponse(
                result.eventId(),
                CdrProcessingStatus.valueOf(result.status().name()));
    }
}
