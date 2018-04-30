package io.apicollab.server.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.CONFLICT;

@ResponseStatus(value = CONFLICT, reason = "Application already exists")
public class ApplicationExistsException extends RuntimeException {

    private static final long serialVersionUID = 7414655805884218727L;

    public ApplicationExistsException(String applicationName) {
        super("Application with name '" + applicationName + "' already exists");
    }
}
