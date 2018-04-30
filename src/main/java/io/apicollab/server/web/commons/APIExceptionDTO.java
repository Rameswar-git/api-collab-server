package io.apicollab.server.web.commons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

/**
 * API Exception DTO with language specific error description.
 */
@EqualsAndHashCode()
@Data
class APIExceptionDTO {

    /**
     * The predefined error type in textual format.
     */
    private String error;

    /**
     * The description of the error.
     */
    private String errorDescription;

    /**
     * The trace id which can be used to debug
     */
    private String traceID;

    /**
     * The status code to the response
     */
    private HttpStatus status;

    @Builder(builderMethodName = "apiExceptionBuilder")
    protected APIExceptionDTO(String errorDescription, String error, HttpStatus status, String traceID) {
        this.errorDescription = errorDescription;
        this.error = error;
        this.status = status;
        this.traceID = traceID;
    }

    @JsonProperty("status")
    public int getRawStatus() {
        return status.value();
    }

    @JsonProperty("status")
    public void setRawStatus(int status) {
        this.status = HttpStatus.valueOf(status);
    }
}
