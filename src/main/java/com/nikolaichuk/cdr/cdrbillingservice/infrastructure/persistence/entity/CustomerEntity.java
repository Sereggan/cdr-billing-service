package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * JPA entity for a customer.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlanEntity subscriptionPlan;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public CustomerEntity(String email, String name, SubscriptionPlanEntity subscriptionPlan, OffsetDateTime createdAt) {
        this.email = email;
        this.name = name;
        this.subscriptionPlan = subscriptionPlan;
        this.createdAt = createdAt;
    }
}
