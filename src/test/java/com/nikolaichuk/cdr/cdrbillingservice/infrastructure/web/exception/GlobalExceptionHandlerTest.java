package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.exception;

import com.nikolaichuk.cdr.cdrbillingservice.application.service.CdrService;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.api.CdrApiController;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.delegate.CdrApiDelegateImpl;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.mapper.CdrMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CdrApiController.class)
@Import({CdrApiDelegateImpl.class, CdrMapper.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

    private static final String CDR_PATH = "/v1/cdr";
    private static final String UNKNOWN_PATH = "/v1/cdr/events";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CdrService cdrService;

    @Test
    @DisplayName("should answer 404 instead of 500 when the endpoint does not exist")
    void shouldReturnNotFoundForUnknownEndpoint() throws Exception {
        mockMvc.perform(post(UNKNOWN_PATH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Endpoint Not Found"))
                .andExpect(jsonPath("$.instance").value(UNKNOWN_PATH));
    }

    @Test
    @DisplayName("should answer 400 instead of 500 when the body carries an unknown usage type")
    void shouldReturnBadRequestForUnreadableBody() throws Exception {
        mockMvc.perform(post(CDR_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                  "customerId": 1,
                                  "type": "TELEPATHY",
                                  "quantity": 1,
                                  "timestamp": "2026-03-19T10:15:30Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Malformed Request"))
                .andExpect(jsonPath("$.instance").value(CDR_PATH));
    }
}
