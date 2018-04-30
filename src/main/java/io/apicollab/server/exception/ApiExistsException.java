package io.apicollab.server.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.CONFLICT;

@ResponseStatus(value = CONFLICT, reason = "Api already exists")
public class ApiExistsException extends RuntimeException {

    private static final long serialVersionUID = -8839562666709594341L;

    public ApiExistsException(String applicationName, String apiName, String apiVersion) {
        super("'" + apiName + "' Api with version '" + apiVersion + "' already exists for application '" + applicationName + "'");
    }
}
