package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerAlreadyExistsException;
import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.application.exception.SubscriptionPlanNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.application.service.CustomerService;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.Customer;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.PlanType;
import com.nikolaichuk.cdr.cdrbillingservice.domain.customer.SubscriptionPlan;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.api.CustomersApiController;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.exception.GlobalExceptionHandler;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper.CustomerWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomersApiController.class)
@Import({CustomersApiDelegateImpl.class, CustomerWebMapper.class, GlobalExceptionHandler.class})
class CustomersApiDelegateTest {

    private static final Long CUSTOMER_ID = 1L;
    private static final String EMAIL = "alice@example.com";
    private static final String NAME = "Alice";
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.of(2026, 3, 19, 10, 15, 30, 0, ZoneOffset.UTC);
    private static final BigDecimal CALL_RATE = new BigDecimal("0.0500");
    private static final BigDecimal SMS_RATE = new BigDecimal("0.0100");
    private static final BigDecimal DATA_RATE = new BigDecimal("2.0000");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Nested
    @DisplayName("POST /v1/customers")
    class RegisterCustomer {

        @Test
        @DisplayName("should register a customer and return 201")
        void shouldRegisterCustomer() throws Exception {
            when(customerService.saveCustomer(eq(EMAIL), eq(NAME), eq(PlanType.STANDARD)))
                    .thenReturn(standardCustomer());

            mockMvc.perform(post("/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerJson(EMAIL, NAME, "STANDARD")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(CUSTOMER_ID))
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andExpect(jsonPath("$.name").value(NAME))
                    .andExpect(jsonPath("$.plan.type").value("STANDARD"));
        }

        @Test
        @DisplayName("should return 400 when email is not valid")
        void shouldReturnBadRequestWhenEmailInvalid() throws Exception {
            mockMvc.perform(post("/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerJson("not-an-email", NAME, "STANDARD")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors[0].field").value("email"));
        }

        @Test
        @DisplayName("should return 404 when the plan does not exist on registration")
        void shouldReturnNotFoundWhenPlanMissing() throws Exception {
            when(customerService.saveCustomer(any(), any(), eq(PlanType.PREMIUM)))
                    .thenThrow(new SubscriptionPlanNotFoundException(PlanType.PREMIUM));

            mockMvc.perform(post("/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerJson(EMAIL, NAME, "PREMIUM")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Subscription Plan Not Found"));
        }

        @Test
        @DisplayName("should return 409 when a customer with the same email already exists")
        void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
            when(customerService.saveCustomer(eq(EMAIL), eq(NAME), eq(PlanType.STANDARD)))
                    .thenThrow(new CustomerAlreadyExistsException(EMAIL));

            mockMvc.perform(post("/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerJson(EMAIL, NAME, "STANDARD")))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.title").value("Customer Already Exists"));
        }
    }

    @Nested
    @DisplayName("GET /v1/customers/{id}")
    class GetCustomer {

        @Test
        @DisplayName("should return a customer by id")
        void shouldGetCustomer() throws Exception {
            when(customerService.getCustomer(CUSTOMER_ID)).thenReturn(standardCustomer());

            mockMvc.perform(get("/v1/customers/{id}", CUSTOMER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(CUSTOMER_ID))
                    .andExpect(jsonPath("$.email").value(EMAIL));
        }

        @Test
        @DisplayName("should return 404 when the customer does not exist")
        void shouldReturnNotFoundWhenCustomerMissing() throws Exception {
            when(customerService.getCustomer(CUSTOMER_ID))
                    .thenThrow(new CustomerNotFoundException(CUSTOMER_ID));

            mockMvc.perform(get("/v1/customers/{id}", CUSTOMER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Customer Not Found"))
                    .andExpect(jsonPath("$.instance").value("/v1/customers/1"));
        }
    }

    private String registerJson(String email, String name, String plan) throws Exception {
        return objectMapper.writeValueAsString(new RegisterPayload(email, name, plan));
    }

    private Customer standardCustomer() {
        return new Customer(CUSTOMER_ID, EMAIL, NAME, standardPlan(), CREATED_AT);
    }

    private SubscriptionPlan standardPlan() {
        return new SubscriptionPlan(1L, PlanType.STANDARD, 0, CALL_RATE, 0, SMS_RATE, DATA_RATE);
    }

    private record RegisterPayload(String email, String name, String plan) {
    }
}
