package io.apicollab.server.mapper;

import io.apicollab.server.domain.Application;
import io.apicollab.server.dto.ApplicationDTO;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationMapperTest {

    private ApplicationMapper applicationMapper = new ApplicationMapper();

    @Test
    public void toDtoWithNullInput() {
        assertThat(applicationMapper.toDto(null)).isNull();
    }

    @Test
    public void toDto() {
        Application application = Application.builder().id("id").name("name").email("mail@mail.com").build();
        ApplicationDTO applicationDTO = ApplicationDTO.builder().applicationId("id").name("name").email("mail@mail.com").build();
        assertThat(applicationMapper.toDto(application)).isEqualToComparingFieldByField(applicationDTO);
    }

    @Test
    public void toDtosWithNullInput() {
        assertThat(applicationMapper.toDtos(null)).isEmpty();
    }

    @Test
    public void toDtos() {
        Application application = Application.builder().id("id").name("name").email("mail@mail.com").build();
        Application application1 = Application.builder().id("id1").name("name1").email("mail1@mail.com").build();
        ApplicationDTO applicationDTO = ApplicationDTO.builder().applicationId("id").name("name").email("mail@mail.com").build();
        ApplicationDTO applicationDTO1 = ApplicationDTO.builder().applicationId("id1").name("name1").email("mail1@mail.com").build();
        Collection<ApplicationDTO> applicationDTOList = applicationMapper.toDtos(Arrays.asList(application, application1));
        assertThat(applicationDTOList).hasSize(2);
        assertThat(applicationDTOList).contains(applicationDTO);
        assertThat(applicationDTOList).contains(applicationDTO1);
    }

    @Test
    public void toEntityWithNullInput() {
        assertThat(applicationMapper.toEntity(null)).isNull();
    }

    @Test
    public void toEntity() {
        Application application = Application.builder().id("id").name("name").email("mail@mail.com").build();
        ApplicationDTO applicationDTO = ApplicationDTO.builder().applicationId("id").name("name").email("mail@mail.com").build();
        assertThat(applicationMapper.toEntity(applicationDTO)).isEqualToComparingFieldByField(application);
    }
}
