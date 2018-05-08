package io.apicollab.server.service;

import io.apicollab.server.constant.ApiStatus;
import io.apicollab.server.domain.Api;
import io.apicollab.server.domain.Application;
import io.apicollab.server.repository.ApplicationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiSearchTests {

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    ApplicationService applicationService;

    @Autowired
    ApiService apiService;

    private Application createApp(String name, String email) {
        Application app = new Application();
        app.setEmail(name);
        app.setName(email);
        app = applicationService.create(app);
        assertThat(app.getId()).isNotBlank();
        return app;
    }

    private Api createApi(Application app, String name, String apiDescription, ApiStatus status) {

        Api api = Api.builder()
                .name(name)
                .version("1")
                .description("A sample description")
                .swaggerDefinition(apiDescription)
                .status(status)
                .build();
        return apiService.create(app, api);

    }

    @Before
    public void setup() {
        // Create application
        Application app = createApp("testApp1", "test1@gmail.com");
        // Create apis with fake descriptions
        createApi(app, "Fruits API", "apple fruit, banana are awesome", ApiStatus.BETA);
        createApi(app, "Space API", "technology space time are interesting concepts", ApiStatus.STABLE);
        createApi(app, "Old API", " Old technology space time are interesting concepts", ApiStatus.ARCHIVED);

    }

    @After
    public void cleanUp() {
        applicationRepository.deleteAll();
    }

    /**
     * Search to get results that return only one result
     */
    @Test
    public void searchSimple() {
        List<Api> results = apiService.search("banana").stream().collect(Collectors.toList());
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualToIgnoringCase("Fruits API");


        results = apiService.search("space").stream().collect(Collectors.toList());
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualToIgnoringCase("Space API");
    }

    /**
     * Search to get results that return two results but one ranked higher
     */
    @Test
    public void searchRelevance() {
        List<Api> results = apiService.search("banana space time").stream().collect(Collectors.toList());
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualToIgnoringCase("Space API");
        assertThat(results.get(1).getName()).isEqualToIgnoringCase("Fruits API");
    }

    /**
     * Search to ensure Archived APIs are not returned.
     */
    @Test
    public void searchExcludeArchived() {
        List<Api> results = apiService.search("space time").stream().collect(Collectors.toList());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualToIgnoringCase("Space API");
    }

}
