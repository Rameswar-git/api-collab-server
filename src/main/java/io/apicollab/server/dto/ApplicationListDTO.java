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
public class ApplicationListDTO implements Serializable {

    private static final long serialVersionUID = -933540596183106170L;

    private Collection<ApplicationDTO> applications;
}
