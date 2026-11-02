package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.exception;

import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerAlreadyExistsException;
import com.nikolaichuk.cdr.cdrbillingservice.application.exception.CustomerNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.application.exception.SubscriptionPlanNotFoundException;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.ErrorResponse;
import com.nikolaichuk.cdr.cdrbillingservice.infrastructure.web.dto.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Translates application and validation exceptions into {@link ErrorResponse} payloads.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex,
                                                                HttpServletRequest request) {
        log.warn("Customer not found on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Customer Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(SubscriptionPlanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlanNotFound(SubscriptionPlanNotFoundException ex,
                                                            HttpServletRequest request) {
        log.warn("Subscription plan not found on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Subscription Plan Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCustomerAlreadyExists(CustomerAlreadyExistsException ex,
                                                                     HttpServletRequest request) {
        log.warn("Customer already exists on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, "Customer Already Exists", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(IllegalArgumentException ex,
                                                            HttpServletRequest request) {
        log.warn("Invalid request on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Invalid Request", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        log.warn("Validation failed on {} {}: {}", request.getMethod(), request.getRequestURI(), fieldErrors);
        ErrorResponse body = baseError(HttpStatus.BAD_REQUEST, "Validation Failed",
                "One or more fields are invalid", request);
        body.setErrors(fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnknownEndpoint(NoResourceFoundException ex,
                                                               HttpServletRequest request) {
        log.warn("Unknown endpoint {} {}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.NOT_FOUND, "Endpoint Not Found", "The requested endpoint does not exist", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        log.warn("Unreadable request body on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Malformed Request", "The request body could not be read", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String title, String detail,
                                                HttpServletRequest request) {
        return ResponseEntity.status(status).body(baseError(status, title, detail, request));
    }

    private ErrorResponse baseError(HttpStatus status, String title, String detail,
                                    HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(status.value(), title);
        body.setDetail(detail);
        body.setInstance(request.getRequestURI());
        return body;
    }
}
