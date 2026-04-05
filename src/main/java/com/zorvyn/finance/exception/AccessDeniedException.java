package com.zorvyn.finance.exception;

/**
 * Thrown when a user tries to do something they don't have permission for.
 * Example: A VIEWER trying to create a transaction.
 *
 * Our GlobalExceptionHandler catches this and returns HTTP 403 Forbidden.
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
