package io.apicollab.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiListDTO implements Serializable {

    private static final long serialVersionUID = 7937487939121387950L;

    private Collection<ApiDTO> apis;
}
