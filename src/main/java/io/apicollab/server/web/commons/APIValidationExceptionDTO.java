package io.apicollab.server.web.commons;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
class APIValidationExceptionDTO extends APIExceptionDTO {
    // Builder annotation is placed on constructor instead of class due to limitation of lombok inheritance support. See: https://github.com/rzwitserloot/lombok/issues/853

    @Getter
    private final List<ValidationResultDTO> validationErrors;

    @Builder(builderMethodName = "validationExceptionBuilder")
    public APIValidationExceptionDTO(String errorDescription, String error, HttpStatus status, String traceID, List<ValidationResultDTO> validationErrors) {
        super(errorDescription, error, status, traceID);
        this.validationErrors = validationErrors;
    }
}
