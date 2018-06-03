package io.apicollab.server.mapper;

import io.apicollab.server.constant.ApiStatus;
import io.apicollab.server.domain.Api;
import io.apicollab.server.dto.ApiDTO;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiMapperTest {

    private ApiMapper apiMapper = new ApiMapper();

    @Test
    public void toDtoWithNullInput() {
        assertThat(apiMapper.toDto(null)).isNull();
    }

    @Test
    public void toDto() {
        Api api = Api.builder().id("id").name("name").version("v1").status(ApiStatus.BETA).description("api description").swaggerDefinition("{}").build();
        ApiDTO apiDTO = ApiDTO.builder().apiId("id").name("name").version("v1").status("BETA").description("api description").swaggerDefinition(null).build();
        assertThat(apiMapper.toDto(api)).isEqualToComparingFieldByField(apiDTO);
    }

    @Test
    public void toDtosWithNullInput() {
        assertThat(apiMapper.toDtos(null)).isEmpty();
    }

    @Test
    public void toDtos() {
        Api api = Api.builder().id("id").name("name").version("v1").status(ApiStatus.BETA).description("api description").swaggerDefinition("{}").build();
        Api api1 = Api.builder().id("id1").name("name1").version("v1").status(ApiStatus.BETA).description("api description1").swaggerDefinition("{}").build();
        ApiDTO apiDTO = ApiDTO.builder().apiId("id").name("name").version("v1").status("BETA").description("api description").swaggerDefinition(null).build();
        ApiDTO apiDTO1 = ApiDTO.builder().apiId("id1").name("name1").version("v1").status("BETA").description("api description1").swaggerDefinition(null).build();
        Collection<ApiDTO> apiDTOs = apiMapper.toDtos(Arrays.asList(api, api1));
        assertThat(apiDTOs).hasSize(2);
        assertThat(apiDTOs).contains(apiDTO);
        assertThat(apiDTOs).contains(apiDTO1);
    }

    @Test
    public void toEntityWithNullInput() {
        assertThat(apiMapper.toEntity(null)).isNull();
    }

    @Test
    public void toEntity() {
        Api api = Api.builder().id(null).name("name").version("v1").status(ApiStatus.BETA).description("api description").swaggerDefinition("{}").build();
        ApiDTO apiDTO = ApiDTO.builder().apiId("id").name("name").version("v1").status("BETA").description("api description").swaggerDefinition("{}").build();
        assertThat(apiMapper.toEntity(apiDTO)).isEqualToComparingFieldByField(api);
    }
}
