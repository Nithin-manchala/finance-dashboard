package com.zorvyn.finance.exception;

import com.zorvyn.finance.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler — a single place that catches all exceptions
 * thrown anywhere in our application and converts them to clean JSON responses.
 *
 * Without this, Spring would return ugly HTML error pages or raw stack traces.
 *
 * @RestControllerAdvice means: "Apply this to all @RestController classes"
 * @ExceptionHandler(X.class) means: "Run this method when exception X is thrown"
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles AccessDeniedException → 403 Forbidden
     * Triggered when a user tries to do something their role doesn't allow.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * Handles ResourceNotFoundException → 404 Not Found
     * Triggered when a requested resource doesn't exist in the database.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * Handles validation failures → 400 Bad Request
     * Triggered when @Valid fails (e.g., missing required fields, wrong format).
     * Collects all validation errors and joins them into one readable message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(MethodArgumentNotValidException e) {
        // Collect all field validation errors into one message
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }

    /**
     * Handles IllegalArgumentException → 400 Bad Request
     * Used for business rule violations like "email already exists".
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * Catch-all for any unexpected exceptions → 500 Internal Server Error
     * This is our safety net — we never want raw errors leaking to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(Exception e) {
        // Log the actual error for debugging (don't expose internal details to client)
        System.err.println("Unexpected error: " + e.getMessage());
        e.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again."));
    }
}
