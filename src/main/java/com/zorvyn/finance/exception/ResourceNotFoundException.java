package com.zorvyn.finance.exception;

/**
 * Thrown when something requested is not found in the database.
 * Example: GET /api/transactions/999 but no transaction with ID 999 exists.
 *
 * Our GlobalExceptionHandler catches this and returns HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
