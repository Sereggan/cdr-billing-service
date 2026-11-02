package com.nikolaichuk.cdr.cdrbillingservice.domain.port;

import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(Long id);

    List<Customer> findAllByIds(Collection<Long> ids);

    List<Customer> findAll();
}
