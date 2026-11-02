package com.nikolaichuk.cdr.cdrbillingservice.application.exception;

/**
 * Thrown when a customer with the same email already exists.
 */
public class CustomerAlreadyExistsException extends RuntimeException {

    public CustomerAlreadyExistsException(String email) {
        super("Customer already exists: " + email);
    }
}
