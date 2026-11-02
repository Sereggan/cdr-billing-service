package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.SubscriptionPlanEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionPlanMapperTest {

    private static final long PLAN_ID = 2L;
    private static final int FREE_CALL_MINUTES = 100;
    private static final int FREE_SMS = 20;
    private static final BigDecimal CALL_RATE = new BigDecimal("0.03");
    private static final BigDecimal SMS_RATE = new BigDecimal("0.01");
    private static final BigDecimal DATA_RATE = new BigDecimal("2.00");

    private final SubscriptionPlanMapper mapper = new SubscriptionPlanMapper();

    @Test
    @DisplayName("toDomain() maps all plan fields")
    void shouldMapAllFieldsToDomain() {
        SubscriptionPlanEntity entity = new SubscriptionPlanEntity();
        entity.setId(PLAN_ID);
        entity.setType(PlanType.PREMIUM);
        entity.setFreeCallMinutes(FREE_CALL_MINUTES);
        entity.setCallRate(CALL_RATE);
        entity.setFreeSms(FREE_SMS);
        entity.setSmsRate(SMS_RATE);
        entity.setDataRate(DATA_RATE);

        SubscriptionPlan plan = mapper.toDomain(entity);

        assertThat(plan.id()).isEqualTo(PLAN_ID);
        assertThat(plan.type()).isEqualTo(PlanType.PREMIUM);
        assertThat(plan.freeCallMinutes()).isEqualTo(FREE_CALL_MINUTES);
        assertThat(plan.callRate()).isEqualByComparingTo(CALL_RATE);
        assertThat(plan.freeSms()).isEqualTo(FREE_SMS);
        assertThat(plan.smsRate()).isEqualByComparingTo(SMS_RATE);
        assertThat(plan.dataRate()).isEqualByComparingTo(DATA_RATE);
    }
}
