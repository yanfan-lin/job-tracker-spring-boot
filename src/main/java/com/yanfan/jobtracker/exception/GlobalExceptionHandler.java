package com.yanfan.jobtracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// a global exception handler for REST API errors
@RestControllerAdvice
public class GlobalExceptionHandler {

    // handles database records not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException e) {
        Map<String, Object> error = buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

    }

    // handles invalid query parameters, such as invalid sort_by, order, limit, or offset
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, Object> error = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // handles @Valid request body validation errors from DTO classes
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> response = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Request body validation failed"
        );

        response.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // helper to keep all error response in the same shape
    private Map<String, Object> buildErrorResponse(int status, String error, String message) {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", status);
        response.put("error", error);
        response.put("message", message);

        return response;
    }

}
