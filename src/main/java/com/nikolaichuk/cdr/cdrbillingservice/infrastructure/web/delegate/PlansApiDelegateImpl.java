package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.delegate;

import com.nikolaichuk.cdr.cdrbillingservice.application.service.SubscriptionPlanService;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.api.PlansApiDelegate;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.PlanResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper.CustomerWebMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegate implementation for the plans API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlansApiDelegateImpl implements PlansApiDelegate {

    private final SubscriptionPlanService subscriptionPlanService;
    private final CustomerWebMapper mapper;

    @Override
    public ResponseEntity<List<PlanResponse>> getPlans() {
        log.info("GET /v1/plans");
        List<PlanResponse> plans = subscriptionPlanService.getPlans().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(plans);
    }
}
