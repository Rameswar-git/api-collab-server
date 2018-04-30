package io.apicollab.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
class ValidationResultDTO implements Serializable {

    private static final long serialVersionUID = 2326034603299614889L;

    private String fieldName;
    private String message;
    private Serializable providedValue;

}
