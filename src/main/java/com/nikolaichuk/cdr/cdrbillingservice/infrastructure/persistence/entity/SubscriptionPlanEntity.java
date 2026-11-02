package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity for a subscription plan.
 */
@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SubscriptionPlanEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, unique = true)
    private PlanType type;

    @Column(name = "free_call_minutes", nullable = false)
    private int freeCallMinutes;

    @Column(name = "call_rate", nullable = false)
    private BigDecimal callRate;

    @Column(name = "free_sms", nullable = false)
    private int freeSms;

    @Column(name = "sms_rate", nullable = false)
    private BigDecimal smsRate;

    @Column(name = "data_rate", nullable = false)
    private BigDecimal dataRate;
}
