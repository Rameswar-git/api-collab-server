package io.apicollab.server.web.commons;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
class ValidationResultDTO implements Serializable {

    private static final long serialVersionUID = 1685248097357276932L;

    private String fieldName;
    private String message;
    private Serializable providedValue;
}
