package com.carmenio.consensus.presentation.middleware;

import lombok.Getter;

import java.time.Instant;

/**
 * Standard API response wrapper for all endpoints.
 * <p>
 * Every controller endpoint returns this structure:
 * <pre>
 * {
 *   "success": true | false,
 *   "message": "Descriptive message",
 *   "data": { ... } | null,
 *   "timestamp": 1234567890
 * }
 * </pre>
 *
 * @param <T> the type of the data payload
 */
@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final long timestamp;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().toEpochMilli();
    }

    /**
     * Creates a success response with data.
     *
     * @param data the response payload
     * @param <T>  the type of the data
     * @return a success ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data);
    }

    /**
     * Creates a success response with a custom message and data.
     *
     * @param message the custom success message
     * @param data    the response payload
     * @param <T>     the type of the data
     * @return a success ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Creates an error response with a message.
     *
     * @param message the error description
     * @param <T>     the type of the data (usually {@code null})
     * @return an error ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * Creates an error response with a message and data.
     *
     * @param message the error description
     * @param data    optional error details
     * @param <T>     the type of the data
     * @return an error ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
