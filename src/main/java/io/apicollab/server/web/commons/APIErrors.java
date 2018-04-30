package io.apicollab.server.web.commons;

import org.springframework.http.HttpStatus;

/**
 * All the possible error codes for a response.
 */
public enum APIErrors {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "One or more parameters failed validation"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Oops! An unexpected error occurred"),
    NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "Resource not found"),
    CONFLICT_ERROR(HttpStatus.CONFLICT, "The request could not be completed due to a conflict");

    public final HttpStatus status;
    public final String message;

    APIErrors(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}