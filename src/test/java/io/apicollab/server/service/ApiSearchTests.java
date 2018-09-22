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

    private Api createApi(Application app, String name, String version, String apiDescription, ApiStatus status) {

        Api api = Api.builder()
                .name(name)
                .version(version)
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
        createApi(app, "Fruits API", "1.0", "apple fruit, banana are awesome", ApiStatus.BETA);
        createApi(app, "Space API", "2.0", "technology space time are interesting concepts this", ApiStatus.STABLE);
        createApi(app, "Old API", "3.0", " Old technology space time are interesting concepts", ApiStatus.ARCHIVED);
        createApi(app, "Tech API", "4.0", "Ban this Tech!", ApiStatus.STABLE);
        createApi(app, "Live API", "5.0", "a live api", ApiStatus.LIVE);


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

    /**
     * Search to ensure Live APIs are returned
     */
    @Test
    public void searchIncludesLive() {
        List<Api> results = apiService.search("live").stream().collect(Collectors.toList());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualToIgnoringCase("Live API");
    }

    /**
     * Case insensitive search
     */
    @Test
    public void searchCaseInsensitiveSearch() {
        List<Api> results = apiService.search("INTERESTING").stream().collect(Collectors.toList());
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualToIgnoringCase("Space API");
    }

    /**
     * Case insensitive search with partial word
     */
    @Test
    public void searchCaseInsensitiveSearchPartialWord() {
        List<Api> results = apiService.search("INTEREST").stream().collect(Collectors.toList());
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualToIgnoringCase("Space API");
    }

    /**
     * Case insensitive search with partial word
     */
    @Test
    public void searchCaseInsensitiveSearchMultiplePartialWord() {
        List<Api> results = apiService.search("baN TECH").stream().collect(Collectors.toList());
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getName()).isEqualToIgnoringCase("Tech API"); // contains both words
        assertThat(results.get(1).getName()).isEqualToIgnoringCase("Fruits API"); // contains ban "bananna"
        assertThat(results.get(2).getName()).isEqualToIgnoringCase("Space API"); // contains technology
    }

    /**
     * Ensure Search phrase is respected
     */
    @Test
    public void searchPhrase() {
        List<Api> results = apiService.search("\"ban this tech\"").stream().collect(Collectors.toList());
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualToIgnoringCase("Tech API");
    }

}
