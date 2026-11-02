package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.CustomerEntity;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.SubscriptionPlanEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();
    private static final String EMAIL = "alice@example.com";
    private static final String NAME = "Alice";
    private static final BigDecimal CALL_RATE = new BigDecimal("0.05");
    private static final BigDecimal SMS_RATE = new BigDecimal("0.01");
    private static final BigDecimal DATA_RATE = new BigDecimal("2.00");

    private final CustomerMapper mapper = new CustomerMapper(new SubscriptionPlanMapper());

    @Test
    @DisplayName("toDomain() maps all customer fields including the plan")
    void shouldMapAllFieldsToDomainIncludingPlan() {
        CustomerEntity entity = new CustomerEntity(EMAIL, NAME, createStandardPlanEntity(), CREATED_AT);
        entity.setId(42L);

        Customer customer = mapper.toDomain(entity);

        assertThat(customer.getId()).isEqualTo(42L);
        assertThat(customer.getEmail()).isEqualTo(EMAIL);
        assertThat(customer.getName()).isEqualTo(NAME);
        assertThat(customer.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(customer.getSubscriptionPlan()).isEqualTo(createStandardPlan());
    }

    @Test
    @DisplayName("toEntity() copies domain fields and attaches the managed plan")
    void shouldCopyDomainFieldsAndAttachManagedPlanToEntity() {
        Customer customer = new Customer(EMAIL, NAME, createStandardPlan(), CREATED_AT);
        SubscriptionPlanEntity managedPlan = createStandardPlanEntity();

        CustomerEntity entity = mapper.toEntity(customer, managedPlan);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getEmail()).isEqualTo(EMAIL);
        assertThat(entity.getName()).isEqualTo(NAME);
        assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(entity.getSubscriptionPlan()).isSameAs(managedPlan);
    }

    private SubscriptionPlanEntity createStandardPlanEntity() {
        SubscriptionPlanEntity entity = new SubscriptionPlanEntity();
        entity.setId(1L);
        entity.setType(PlanType.STANDARD);
        entity.setFreeCallMinutes(0);
        entity.setCallRate(CALL_RATE);
        entity.setFreeSms(0);
        entity.setSmsRate(SMS_RATE);
        entity.setDataRate(DATA_RATE);
        return entity;
    }

    private SubscriptionPlan createStandardPlan() {
        return new SubscriptionPlan(1L, PlanType.STANDARD, 0, CALL_RATE, 0, SMS_RATE, DATA_RATE);
    }
}
