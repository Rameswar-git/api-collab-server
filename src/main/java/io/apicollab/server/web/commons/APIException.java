package io.apicollab.server.web.commons;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * API Exceptions to be thrown by API Endpoints.
 */

@SuppressWarnings("serial")
public class APIException extends RuntimeException {

    @Getter
    protected final String error;

    @Getter
    protected final HttpStatus status;

    @Getter
    protected final String message;

    public APIException(String errorDescription, String error, HttpStatus status) {
        super(errorDescription);
        this.error = error;
        this.status = status;
        this.message = errorDescription;
    }

    public APIException(APIErrors type) {
        this(type.message, type.name(), type.status);
    }
}
