package io.apicollab.server.mapper;

import io.apicollab.server.domain.Application;
import io.apicollab.server.dto.ApplicationDTO;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class ApplicationMapper {

    public ApplicationDTO toDto(Application application) {
        if (application == null) {
            return null;
        }
        return ApplicationDTO.builder()
                .applicationId(application.getId())
                .name(application.getName())
                .email(application.getEmail())
                .build();
    }

    public Collection<ApplicationDTO> toDtos(Collection<Application> applications) {
        if (applications == null) {
            return null;
        }
        return applications.stream().map(this::toDto).collect(Collectors.toList());
    }

    public Application toEntity(ApplicationDTO applicationDTO) {
        if (applicationDTO == null) {
            return null;
        }
        return Application.builder()
                .id(applicationDTO.getApplicationId())
                .name(applicationDTO.getName())
                .email(applicationDTO.getEmail())
                .build();
    }
}
