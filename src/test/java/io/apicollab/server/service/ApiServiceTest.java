package io.apicollab.server.service;

import io.apicollab.server.domain.Api;
import io.apicollab.server.domain.Application;
import io.apicollab.server.exception.ApiExistsException;
import io.apicollab.server.repository.ApiRepository;
import io.apicollab.server.repository.ApplicationRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApiService apiService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApiRepository apiRepository;

    @After
    public void cleanup() {
        apiRepository.deleteAll();
        applicationRepository.deleteAll();
    }

    @Test
    public void createNew() {
        // Create an application
        Application application = Application.builder().name("Application_1").email("app1@appcompany.com").build();
        Application dbApplication = applicationService.create(application);
        Api api = Api.builder().name("Api_1").version("0.1").swaggerDefinition("{}").description("a description").build();
        Api dbApi = applicationService.createNewApiVersion(dbApplication.getId(), api);

        // Retrieve created application
        dbApi = apiService.findOne(dbApi.getId());
        assertThat(dbApi.getId()).isNotNull();
        assertThat(dbApi.getName()).isEqualTo(api.getName());
        assertThat(dbApi.getVersion()).isEqualTo(api.getVersion());
        assertThat(dbApi.getSwaggerDefinition()).isEqualTo(api.getSwaggerDefinition());
        assertThat(dbApi.getApplication().getId()).isEqualTo(dbApplication.getId());
    }

    @Test
    public void descriptionTruncatedTo255Chars() {
        String description = "looooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                "ng description";
        // Create an application
        Application application = Application.builder().name("Application_1").email("app1@appcompany.com").build();
        Application dbApplication = applicationService.create(application);
        Api api = Api.builder().name("Api_1").version("0.1").swaggerDefinition("{}").description(description).build();
        Api dbApi = applicationService.createNewApiVersion(dbApplication.getId(), api);

        // Retrieve created application
        dbApi = apiService.findOne(dbApi.getId());
        assertThat(dbApi.getDescription().length()).isEqualTo(255);
    }

    @Test
    public void createDuplicate() {
        // Create an application
        Application application = Application.builder().name("Application_1").email("app1@appcompany.com").build();
        Application dbApplication = applicationService.create(application);
        Api api = Api.builder().name("Api_1").version("0.1").description("a description").swaggerDefinition("{}").build();
        applicationService.createNewApiVersion(dbApplication.getId(), api);

        // Create another Api with same name and version
        Api anotherApi = Api.builder().name("Api_1").version("0.1").description("another description").swaggerDefinition("{}").build();
        assertThatExceptionOfType(ApiExistsException.class).isThrownBy(() -> applicationService.createNewApiVersion(application.getId(), anotherApi));
    }

    @Test
    public void createMultipleWithDifferentNames() {
        // Create an application
        Application application = Application.builder().name("Application_1").email("app1@appcompany.com").build();
        Application dbApplication = applicationService.create(application);
        Api api = Api.builder().name("Api_1").version("0.1").description("a description").swaggerDefinition("{}").build();
        applicationService.createNewApiVersion(dbApplication.getId(), api);

        // Create another Api with another name but same version
        Api anotherApi = Api.builder().name("Api_2").version("0.1").description("a description").swaggerDefinition("{}").build();
        applicationService.createNewApiVersion(dbApplication.getId(), anotherApi);
        assertThat(apiRepository.count()).isEqualTo(2);
    }

    @Test
    public void createMultipleVersions() {
        // Create an application
        Application application = Application.builder().name("Application_1").email("app1@appcompany.com").build();
        Application dbApplication = applicationService.create(application);
        Api api = Api.builder().name("Api_1").version("0.1").description("a description").swaggerDefinition("{}").build();
        applicationService.createNewApiVersion(dbApplication.getId(), api);

        // Create another Api with another name but same version
        Api anotherApi = Api.builder().name("Api_1").version("0.2").description("a description").swaggerDefinition("{}").build();
        applicationService.createNewApiVersion(dbApplication.getId(), anotherApi);
        assertThat(apiRepository.count()).isEqualTo(2);
    }

    @Test
    public void createAPIWithTags() {
        // Create an application
        Application application = Application.builder().name("Application_1").email("app1@appcompany.com").build();
        Application dbApplication = applicationService.create(application);
        Api api = Api.builder().name("Api_1").version("0.1").description("a description").tags(asList("tag1")).swaggerDefinition("{}").build();
        applicationService.createNewApiVersion(dbApplication.getId(), api);
        assertThat(apiRepository.count()).isEqualTo(1);

        List<Api> apis = new ArrayList<>(apiService.findByApplication(dbApplication.getId()));
        Api api2 = apis.get(0);
        assertThat(api2).isNotNull();
        assertThat(api2.getTags()).isNotEmpty();
    }
}
