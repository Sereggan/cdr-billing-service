package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa;

import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.CustomerBillingCycleEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface CustomerBillingCycleJpaRepository extends JpaRepository<CustomerBillingCycleEntity, Long> {

    Optional<CustomerBillingCycleEntity> findByCustomerIdAndCycle(Long customerId, YearMonth cycle);

    @Modifying
    @Query(
            value = """
                    INSERT INTO customer_billing_cycles (
                        customer_id,
                        cycle,
                        total_cost,
                        used_free_seconds,
                        used_free_sms
                    )
                    VALUES (:customerId, :cycle, 0, 0, 0)
                    ON CONFLICT (customer_id, cycle) DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(@Param("customerId") Long customerId, @Param("cycle") String cycle);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT cbc
            FROM CustomerBillingCycleEntity cbc
            WHERE cbc.customerId = :customerId
              AND cbc.cycle = :cycle
            """)
    Optional<CustomerBillingCycleEntity> findForUpdate(
            @Param("customerId") Long customerId,
            @Param("cycle") YearMonth cycle
    );

    /**
     * Ranks the billing cycles by total cost. The customer id tie-break keeps the ranking stable
     * across calls when several customers share the same total cost.
     */
    List<CustomerBillingCycleEntity> findByCycleOrderByTotalCostDescCustomerIdAsc(YearMonth cycle, Pageable pageable);
}
