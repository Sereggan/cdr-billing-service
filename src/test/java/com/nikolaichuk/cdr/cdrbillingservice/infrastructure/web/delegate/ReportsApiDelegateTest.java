package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.delegate;

import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.FreeQuotas;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.TopCustomer;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.TopCustomers;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.UnbilledCharges;
import com.nikolaichuk.cdr.cdrbillingservice.application.service.ReportingService;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.api.ReportsApiController;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.exception.GlobalExceptionHandler;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper.ReportWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportsApiController.class)
@Import({ReportsApiDelegateImpl.class, ReportWebMapper.class, GlobalExceptionHandler.class})
class ReportsApiDelegateTest {

    private static final Long CUSTOMER_ID = 1L;
    private static final String CYCLE = "2026-03";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportingService reportingService;

    @Nested
    @DisplayName("GET /v1/customers/{customerId}/unbilled-charges")
    class GetUnbilledCharges {

        @Test
        @DisplayName("should return the unbilled charges of the current cycle")
        void shouldReturnUnbilledCharges() throws Exception {
            when(reportingService.getUnbilledCharges(CUSTOMER_ID))
                    .thenReturn(new UnbilledCharges(CUSTOMER_ID, new BigDecimal("12.34000000"), CYCLE));

            mockMvc.perform(get("/v1/customers/{customerId}/unbilled-charges", CUSTOMER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID))
                    .andExpect(jsonPath("$.totalCost").value(12.34))
                    .andExpect(jsonPath("$.billingCycle").value(CYCLE));
        }

        @Test
        @DisplayName("should round the cost to currency precision")
        void shouldRoundCostToCurrencyPrecision() throws Exception {
            when(reportingService.getUnbilledCharges(CUSTOMER_ID))
                    .thenReturn(new UnbilledCharges(CUSTOMER_ID, new BigDecimal("0.12500000"), CYCLE));

            mockMvc.perform(get("/v1/customers/{customerId}/unbilled-charges", CUSTOMER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCost").value(0.13));
        }

        @Test
        @DisplayName("should return 404 when the customer does not exist")
        void shouldReturnNotFoundWhenCustomerMissing() throws Exception {
            when(reportingService.getUnbilledCharges(CUSTOMER_ID))
                    .thenThrow(new CustomerNotFoundException(CUSTOMER_ID));

            mockMvc.perform(get("/v1/customers/{customerId}/unbilled-charges", CUSTOMER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Customer Not Found"))
                    .andExpect(jsonPath("$.instance").value("/v1/customers/1/unbilled-charges"));
        }
    }

    @Nested
    @DisplayName("GET /v1/customers/{customerId}/free-quotas")
    class GetFreeQuotas {

        @Test
        @DisplayName("should return the remaining free quotas of the current cycle")
        void shouldReturnFreeQuotas() throws Exception {
            when(reportingService.getFreeQuotas(CUSTOMER_ID))
                    .thenReturn(new FreeQuotas(CUSTOMER_ID, 98, 15, CYCLE));

            mockMvc.perform(get("/v1/customers/{customerId}/free-quotas", CUSTOMER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID))
                    .andExpect(jsonPath("$.freeCallMinutes").value(98))
                    .andExpect(jsonPath("$.freeSms").value(15))
                    .andExpect(jsonPath("$.billingCycle").value(CYCLE));
        }

        @Test
        @DisplayName("should return 404 when the customer does not exist")
        void shouldReturnNotFoundWhenCustomerMissing() throws Exception {
            when(reportingService.getFreeQuotas(CUSTOMER_ID))
                    .thenThrow(new CustomerNotFoundException(CUSTOMER_ID));

            mockMvc.perform(get("/v1/customers/{customerId}/free-quotas", CUSTOMER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Customer Not Found"));
        }
    }

    @Nested
    @DisplayName("GET /v1/reports/top-customers")
    class GetTopCustomers {

        @Test
        @DisplayName("should return the ranking in the order provided by the service")
        void shouldReturnRanking() throws Exception {
            when(reportingService.getTopCustomers()).thenReturn(new TopCustomers(CYCLE, List.of(
                    new TopCustomer(1L, "Alice", "alice@example.com", new BigDecimal("30.00000000")),
                    new TopCustomer(2L, "Bob", "bob@example.com", new BigDecimal("20.00000000")))));

            mockMvc.perform(get("/v1/reports/top-customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.billingCycle").value(CYCLE))
                    .andExpect(jsonPath("$.customers.length()").value(2))
                    .andExpect(jsonPath("$.customers[0].customerId").value(1))
                    .andExpect(jsonPath("$.customers[0].name").value("Alice"))
                    .andExpect(jsonPath("$.customers[0].email").value("alice@example.com"))
                    .andExpect(jsonPath("$.customers[0].totalCost").value(30.00))
                    .andExpect(jsonPath("$.customers[1].customerId").value(2));
        }

        @Test
        @DisplayName("should return an empty ranking when nobody had traffic")
        void shouldReturnEmptyRanking() throws Exception {
            when(reportingService.getTopCustomers()).thenReturn(new TopCustomers(CYCLE, List.of()));

            mockMvc.perform(get("/v1/reports/top-customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.billingCycle").value(CYCLE))
                    .andExpect(jsonPath("$.customers").isEmpty());
        }
    }
}
