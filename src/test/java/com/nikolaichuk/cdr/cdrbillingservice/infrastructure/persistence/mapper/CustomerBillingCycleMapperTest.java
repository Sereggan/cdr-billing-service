package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.mapper;

import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.BillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.domain.billing.CustomerBillingCycle;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.CustomerBillingCycleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerBillingCycleMapperTest {

    private static final Long ID = 7L;
    private static final Long CUSTOMER_ID = 42L;
    private static final YearMonth CYCLE = YearMonth.of(2026, 3);
    private static final BigDecimal TOTAL_COST = new BigDecimal("12.34567800");
    private static final int USED_FREE_SECONDS = 1800;
    private static final int USED_FREE_SMS = 5;

    private final CustomerBillingCycleMapper mapper = new CustomerBillingCycleMapper();

    @Test
    @DisplayName("toDomain() maps all fields")
    void shouldMapAllFieldsToDomain() {
        CustomerBillingCycleEntity entity =
                new CustomerBillingCycleEntity(ID, CUSTOMER_ID, CYCLE, TOTAL_COST, USED_FREE_SECONDS, USED_FREE_SMS);

        CustomerBillingCycle domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(ID);
        assertThat(domain.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(domain.getCycle().value()).isEqualTo(CYCLE);
        assertThat(domain.getTotalCost()).isEqualByComparingTo(TOTAL_COST);
        assertThat(domain.getUsedFreeSeconds()).isEqualTo(USED_FREE_SECONDS);
        assertThat(domain.getUsedFreeSms()).isEqualTo(USED_FREE_SMS);
    }

    @Test
    @DisplayName("toEntity() copies domain fields including the identity")
    void shouldCopyDomainFieldsToEntity() {
        CustomerBillingCycle domain = new CustomerBillingCycle(
                ID, CUSTOMER_ID, new BillingCycle(CYCLE), TOTAL_COST, USED_FREE_SECONDS, USED_FREE_SMS);

        CustomerBillingCycleEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(entity.getCycle()).isEqualTo(CYCLE);
        assertThat(entity.getTotalCost()).isEqualByComparingTo(TOTAL_COST);
        assertThat(entity.getUsedFreeSeconds()).isEqualTo(USED_FREE_SECONDS);
        assertThat(entity.getUsedFreeSms()).isEqualTo(USED_FREE_SMS);
    }
}
