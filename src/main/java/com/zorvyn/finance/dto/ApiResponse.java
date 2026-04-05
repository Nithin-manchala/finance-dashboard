package com.zorvyn.finance.dto;

/**
 * A generic wrapper for all API responses.
 *
 * Why do we use this? Because it gives every response a consistent structure:
 *   {
 *     "success": true,
 *     "message": "Transaction created",
 *     "data": { ... }
 *   }
 *
 * The <T> is a "generic type" — it means 'data' can hold any type:
 *   ApiResponse<User>        → data will be a User object
 *   ApiResponse<List<...>>  → data will be a list
 *   ApiResponse<Map<...>>   → data will be a map (for summaries)
 *   ApiResponse<?>          → data type is unknown (used for error responses)
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // Private constructor: Force use of static factory methods below
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // ── Static Factory Methods ────────────────────────────────────────────────

    /** Call this when an operation succeeds and you have data to return. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /** Call this when an operation succeeds but there's no data to return. */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /** Call this when something goes wrong — returns just the error message. */
    public static ApiResponse<?> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
