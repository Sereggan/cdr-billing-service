package com.nikolaichuk.cdr.cdrbillingservice.application.exception;

/**
 * Thrown when a customer cannot be found by its identifier.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("Customer not found: " + customerId);
    }
}
