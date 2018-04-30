package io.apicollab.server.web.commons;

import brave.Tracer;
import io.apicollab.server.exception.ApiExistsException;
import io.apicollab.server.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Processes all uncaught exceptions.
 */
@ControllerAdvice
class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @Autowired
    private Tracer tracer;

    /**
     * Catch API Exceptions thrown by the developer and return a developer friendly response.
     *
     * @param ex exception to be handled
     * @return valid http response describing the error
     */
    @ExceptionHandler(APIException.class)
    private ResponseEntity<Object> handleAPIException(APIException ex) {

        String description = ex.getMessage();
        APIExceptionDTO err = APIExceptionDTO.apiExceptionBuilder()
                .error(ex.getError())
                .status(ex.getStatus())
                .errorDescription(description)
                .traceID(tracer.currentSpan().context().traceIdString())
                .build();

        //Set the status code with payload.
        return new ResponseEntity<>(err, new HttpHeaders(), err.getStatus());
    }

    /**
     * Catch API Validation Exceptions thrown by the developer and return a developer friendly response.
     *
     * @param ex exception to be handled
     * @return valid http response describing the error
     */
    @ExceptionHandler(APIValidationException.class)
    private ResponseEntity<APIValidationExceptionDTO> handleAPIValidationException(APIValidationException ex) {

        String description = ex.getMessage();
        APIValidationExceptionDTO err = APIValidationExceptionDTO.validationExceptionBuilder()
                .error(ex.error)
                .errorDescription(description)
                .status(ex.status)
                .validationErrors(ex.getValidationErrors())
                .traceID(tracer.currentSpan().context().traceIdString())
                .build();

        return new ResponseEntity<>(err, new HttpHeaders(), err.getStatus());
    }

    /**
     * Catch Exceptions thrown by input validation errors e.g @NotNull
     * This is converted to APIValidation exception
     *
     * @param ex exception to be handled
     * @return valid http response describing the error
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<APIValidationExceptionDTO> handleBindException(BindException ex) {

        List<ValidationResultDTO> fieldErrors = ex.getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(v -> new ValidationResultDTO(v.getField(), v.getDefaultMessage(), (Serializable) v.getRejectedValue()))
                .collect(Collectors.toList());
        APIValidationException apiValidationException = new APIValidationException(APIErrors.VALIDATION_ERROR, fieldErrors);
        return handleAPIValidationException(apiValidationException);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIValidationExceptionDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<ValidationResultDTO> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(v -> new ValidationResultDTO(v.getField(), v.getDefaultMessage(), (Serializable) v.getRejectedValue()))
                .collect(Collectors.toList());
        APIValidationException apiValidationException = new APIValidationException(APIErrors.VALIDATION_ERROR, fieldErrors);
        return handleAPIValidationException(apiValidationException);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<APIValidationExceptionDTO> handleMissingServletRequestPartException(MissingServletRequestPartException ex, WebRequest request) {
        ValidationResultDTO validationResultDTO = new ValidationResultDTO(ex.getRequestPartName(), ex.getMessage(), null);
        APIValidationException apiValidationException = new APIValidationException(APIErrors.VALIDATION_ERROR, Collections.singletonList(validationResultDTO));
        return handleAPIValidationException(apiValidationException);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(NotFoundException ex) {
        if (log.isErrorEnabled()) {
            log.error("Uncaught Exception on traceID: {}" + tracer.currentSpan().context().traceIdString(), ex);
        }
        return handleAPIException(new APIException(APIErrors.NOT_FOUND_ERROR));
    }

    /**
     * Catch all other exceptions and treat as a internal server error exception
     *
     * @param ex exception to be handled
     * @return valid http response describing the error
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleUncaughtException(Exception ex) {
        if (log.isErrorEnabled()) {
            log.error("Uncaught Exception on traceID: {}" + tracer.currentSpan().context().traceIdString(), ex);
        }
        return handleAPIException(new APIException(APIErrors.SERVER_ERROR));
    }

    @ExceptionHandler(ApiExistsException.class)
    public ResponseEntity<Object> handleApiExistsException(ApiExistsException ex) {
        if (log.isErrorEnabled()) {
            log.error("Uncaught Exception on traceID: {}" + tracer.currentSpan().context().traceIdString(), ex);
        }
        return handleAPIException(new APIException(ex.getMessage(), APIErrors.CONFLICT_ERROR.name(), APIErrors.CONFLICT_ERROR.status));
    }
}
