package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity;

import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.converter.YearMonthAttributeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * JPA entity for a customer billing cycle.
 */
@Entity
@Table(name = "customer_billing_cycles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomerBillingCycleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @EqualsAndHashCode.Include
    @Column(name = "cycle", nullable = false, length = 7)
    @Convert(converter = YearMonthAttributeConverter.class)
    private YearMonth cycle;

    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost;

    @Column(name = "used_free_seconds", nullable = false)
    private int usedFreeSeconds;

    @Column(name = "used_free_sms", nullable = false)
    private int usedFreeSms;

    public CustomerBillingCycleEntity(Long id, Long customerId, YearMonth cycle, BigDecimal totalCost, int usedFreeSeconds, int usedFreeSms) {
        this.id = id;
        this.customerId = customerId;
        this.cycle = cycle;
        this.totalCost = totalCost;
        this.usedFreeSeconds = usedFreeSeconds;
        this.usedFreeSms = usedFreeSms;
    }

}
