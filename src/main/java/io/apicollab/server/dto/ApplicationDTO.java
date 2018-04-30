package io.apicollab.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.ResourceSupport;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDTO extends ResourceSupport implements Serializable {

    private static final long serialVersionUID = -1637562419698960215L;

    @JsonProperty("id")
    private String applicationId;
    @NotBlank(message = "Application name cannot be empty")
    private String name;
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Must be a valid email")
    private String email;
    private Collection<ApiDTO> apis;
}
