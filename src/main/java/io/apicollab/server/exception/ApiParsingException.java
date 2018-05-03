package io.apicollab.server.exception;

import lombok.Getter;

import java.util.List;

public class ApiParsingException extends RuntimeException {

    @Getter
    private List<String> errorMessages;
    public ApiParsingException(String message, List<String> errorMessages){
        super(message);
        this.errorMessages = errorMessages;
    }
}
