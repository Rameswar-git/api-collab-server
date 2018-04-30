package io.apicollab.server.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(value = NOT_FOUND, reason = "Resource not found")
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 7502804023844395674L;

    public NotFoundException() {
    }
}
