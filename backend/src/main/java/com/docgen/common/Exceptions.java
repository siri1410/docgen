package com.docgen.common;

/**
 * Domain exceptions used across the platform.
 */
public final class Exceptions {

    private Exceptions() {}

    /** Thrown when a requested entity does not exist. Maps to HTTP 404. */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }

        public static NotFoundException of(String entity, Object id) {
            return new NotFoundException(entity + " not found: " + id);
        }
    }

    /** Thrown when a request is semantically invalid (bad state transition, validation, etc.). Maps to HTTP 400. */
    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }

    /** Thrown when an upstream connector / prefill call fails. Maps to HTTP 502. */
    public static class ConnectorException extends RuntimeException {
        public ConnectorException(String message) {
            super(message);
        }

        public ConnectorException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** Thrown when a rate limit is exceeded. Maps to HTTP 429. */
    public static class RateLimitedException extends RuntimeException {
        public RateLimitedException(String message) {
            super(message);
        }
    }
}
