package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.adapter;

import com.nikolaichuk.cdr.cdrbillingservice.TestcontainersConfiguration;
import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerAlreadyExistsException;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.jpa.CustomerJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link CustomerRepositoryAdapter}: identity generation, plan wiring,
 * lookups and the unique email constraint.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CustomerRepositoryAdapterTest {

    private static final String EMAIL = "alice@example.com";
    private static final String OTHER_EMAIL = "bob@example.com";
    private static final String NAME = "Alice";
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();

    @Autowired
    private CustomerRepositoryAdapter adapter;

    @Autowired
    private SubscriptionPlanRepositoryAdapter planAdapter;

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    @AfterEach
    void cleanUp() {
        customerJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("should save a new customer and generate its identity")
    void shouldSaveCustomerAndGenerateIdentity() {
        Customer saved = adapter.save(createCustomer(EMAIL));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getName()).isEqualTo(NAME);
        assertThat(saved.getCreatedAt().toInstant()).isEqualTo(CREATED_AT.toInstant());
    }

    @Test
    @DisplayName("should attach the subscription plan when saving a customer")
    void shouldAttachSubscriptionPlanWhenSaving() {
        Customer saved = adapter.save(createCustomer(EMAIL));

        assertThat(saved.getSubscriptionPlan().id()).isEqualTo(standardPlan().id());
        assertThat(saved.getSubscriptionPlan().type()).isEqualTo(PlanType.STANDARD);
    }

    @Test
    @DisplayName("should find a saved customer by id")
    void shouldFindCustomerById() {
        Long id = adapter.save(createCustomer(EMAIL)).getId();

        Optional<Customer> result = adapter.findById(id);

        assertThat(result).hasValueSatisfying(customer -> {
            assertThat(customer.getId()).isEqualTo(id);
            assertThat(customer.getEmail()).isEqualTo(EMAIL);
            assertThat(customer.getName()).isEqualTo(NAME);
            assertThat(customer.getSubscriptionPlan().type()).isEqualTo(PlanType.STANDARD);
        });
    }

    @Test
    @DisplayName("should return empty when no customer has the given id")
    void shouldReturnEmptyWhenCustomerNotFound() {
        Optional<Customer> result = adapter.findById(-1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return all saved customers")
    void shouldReturnAllCustomers() {
        adapter.save(createCustomer(EMAIL));
        adapter.save(createCustomer(OTHER_EMAIL));

        List<Customer> result = adapter.findAll();

        assertThat(result)
                .extracting(Customer::getEmail)
                .containsExactlyInAnyOrder(EMAIL, OTHER_EMAIL);
    }

    @Test
    @DisplayName("should throw when saving a customer with an already used email")
    void shouldThrowWhenEmailAlreadyExists() {
        adapter.save(createCustomer(EMAIL));

        assertThatThrownBy(() -> adapter.save(createCustomer(EMAIL)))
                .isInstanceOf(CustomerAlreadyExistsException.class);
    }

    private Customer createCustomer(String email) {
        return new Customer(email, NAME, standardPlan(), CREATED_AT);
    }

    private SubscriptionPlan standardPlan() {
        return planAdapter.findByType(PlanType.STANDARD).orElseThrow();
    }
}
