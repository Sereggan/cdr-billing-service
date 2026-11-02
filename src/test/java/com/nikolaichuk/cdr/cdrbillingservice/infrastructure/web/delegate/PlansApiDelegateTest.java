package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.delegate;

import com.nikolaichuk.cdr.cdrbillingservice.application.service.SubscriptionPlanService;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.api.PlansApiController;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.exception.GlobalExceptionHandler;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper.CustomerWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlansApiController.class)
@Import({PlansApiDelegateImpl.class, CustomerWebMapper.class, GlobalExceptionHandler.class})
class PlansApiDelegateTest {

    private static final BigDecimal STANDARD_CALL_RATE = new BigDecimal("0.0500");
    private static final BigDecimal PREMIUM_CALL_RATE = new BigDecimal("0.0300");
    private static final BigDecimal SMS_RATE = new BigDecimal("0.0100");
    private static final BigDecimal DATA_RATE = new BigDecimal("2.0000");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionPlanService subscriptionPlanService;

    @Test
    @DisplayName("should return all plans")
    void shouldListPlans() throws Exception {
        when(subscriptionPlanService.getPlans()).thenReturn(List.of(standardPlan(), premiumPlan()));

        mockMvc.perform(get("/v1/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("STANDARD"))
                .andExpect(jsonPath("$[1].type").value("PREMIUM"))
                .andExpect(jsonPath("$[1].freeCallMinutes").value(100));
    }

    private SubscriptionPlan standardPlan() {
        return new SubscriptionPlan(1L, PlanType.STANDARD, 0, STANDARD_CALL_RATE, 0, SMS_RATE, DATA_RATE);
    }

    private SubscriptionPlan premiumPlan() {
        return new SubscriptionPlan(2L, PlanType.PREMIUM, 100, PREMIUM_CALL_RATE, 20, SMS_RATE, DATA_RATE);
    }
}
