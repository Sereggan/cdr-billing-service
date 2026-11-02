package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa;

import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {

    Optional<CustomerEntity> findByEmail(String email);
}
