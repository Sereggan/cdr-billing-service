package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.delegate;

import com.nikolaichuk.cdr.cdrbillingservice.application.model.CdrProcessingResult;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.CdrProcessingStatus;
import com.nikolaichuk.cdr.cdrbillingservice.application.service.CdrService;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.api.CdrApiDelegate;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.CdrRequest;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.CdrResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper.CdrMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Web adapter for the CDR API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CdrApiDelegateImpl implements CdrApiDelegate {

    private final CdrService cdrService;
    private final CdrMapper mapper;

    @Override
    public ResponseEntity<CdrResponse> submitCdr(CdrRequest cdrRequest) {
        log.info("POST /v1/cdr eventId={} customerId={} type={} quantity={}",
                cdrRequest.getEventId(), cdrRequest.getCustomerId(),
                cdrRequest.getType(), cdrRequest.getQuantity());
        CdrProcessingResult result = cdrService.process(mapper.toDomain(cdrRequest));
        HttpStatus status = result.status() == CdrProcessingStatus.DUPLICATE
                ? HttpStatus.OK
                : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(mapper.toResponse(result));
    }
}
