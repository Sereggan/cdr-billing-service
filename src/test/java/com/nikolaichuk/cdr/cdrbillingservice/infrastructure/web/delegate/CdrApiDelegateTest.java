package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.CdrProcessingResult;
import com.nikolaichuk.cdr.cdrbillingservice.application.model.CdrProcessingStatus;
import com.nikolaichuk.cdr.cdrbillingservice.application.service.CdrService;
import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.CdrEvent;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.api.CdrApiController;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.exception.GlobalExceptionHandler;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper.CdrMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CdrApiController.class)
@Import({CdrApiDelegateImpl.class, CdrMapper.class, GlobalExceptionHandler.class})
class CdrApiDelegateTest {

    private static final UUID EVENT_ID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    private static final Long CUSTOMER_ID = 1L;
    private static final OffsetDateTime TIMESTAMP = OffsetDateTime.of(2026, 3, 19, 10, 15, 30, 0, ZoneOffset.UTC);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CdrService cdrService;

    @Test
    @DisplayName("should process a new event and return 201")
    void shouldProcessNewEvent() throws Exception {
        when(cdrService.process(any(CdrEvent.class)))
                .thenReturn(new CdrProcessingResult(EVENT_ID, CdrProcessingStatus.PROCESSED));

        mockMvc.perform(post("/v1/cdr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cdrJson(EVENT_ID, CUSTOMER_ID, "CALL", 60)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.status").value("PROCESSED"));
    }

    @Test
    @DisplayName("should acknowledge a duplicate event and return 200")
    void shouldAcknowledgeDuplicateEvent() throws Exception {
        when(cdrService.process(any(CdrEvent.class)))
                .thenReturn(new CdrProcessingResult(EVENT_ID, CdrProcessingStatus.DUPLICATE));

        mockMvc.perform(post("/v1/cdr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cdrJson(EVENT_ID, CUSTOMER_ID, "CALL", 60)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.status").value("DUPLICATE"));
    }

    @Test
    @DisplayName("should return 400 when a required field is missing")
    void shouldReturnBadRequestWhenFieldMissing() throws Exception {
        mockMvc.perform(post("/v1/cdr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cdrJson(null, CUSTOMER_ID, "CALL", 60)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("eventId"));
    }

    @Test
    @DisplayName("should return 400 when quantity is negative")
    void shouldReturnBadRequestWhenQuantityNegative() throws Exception {
        mockMvc.perform(post("/v1/cdr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cdrJson(EVENT_ID, CUSTOMER_ID, "CALL", -1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("quantity"));
    }

    @Test
    @DisplayName("should return 404 when the customer does not exist")
    void shouldReturnNotFoundWhenCustomerMissing() throws Exception {
        when(cdrService.process(any(CdrEvent.class)))
                .thenThrow(new CustomerNotFoundException(CUSTOMER_ID));

        mockMvc.perform(post("/v1/cdr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cdrJson(EVENT_ID, CUSTOMER_ID, "CALL", 60)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Customer Not Found"));
    }

    private String cdrJson(UUID eventId, Long customerId, String type, int quantity) throws Exception {
        return objectMapper.writeValueAsString(new CdrPayload(eventId, customerId, type, quantity, TIMESTAMP));
    }

    private record CdrPayload(UUID eventId, Long customerId, String type, int quantity, OffsetDateTime timestamp) {
    }
}
