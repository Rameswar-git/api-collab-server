package io.apicollab.server.web.commons;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

public class APIValidationException extends APIException {

    private static final long serialVersionUID = -7423209109074845495L;

    @Getter
    private final transient List<ValidationResultDTO> validationErrors;

    public APIValidationException(String description, String error, HttpStatus status, List<ValidationResultDTO> validationErrors) {
        super(description, error, status);
        this.validationErrors = validationErrors;
    }

    public APIValidationException(APIErrors type, List<ValidationResultDTO> validationErrors) {
        this(type.message, type.name(), type.status, validationErrors);
    }
}
