package io.apicollab.server.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ResponseStatus(value = INTERNAL_SERVER_ERROR, reason = "Internal server error")
public class ApiPortalException extends RuntimeException {

    private static final long serialVersionUID = 1090587313257014505L;

    public ApiPortalException(String msg) {
        super(msg);
    }
}
