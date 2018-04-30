package io.apicollab.server.service;

import io.apicollab.server.domain.Application;
import io.apicollab.server.exception.ApplicationExistsException;
import io.apicollab.server.repository.ApplicationRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @After
    public void cleanup() {
        applicationRepository.deleteAll();
    }

    @Test
    public void createNew() {
        // Create an application
        Application application = Application.builder().name("Application_1").email("app1@appcompany.com").build();
        Application dbApplication = applicationService.create(application);

        // Retrieve created application
        dbApplication = applicationService.findById(dbApplication.getId());
        assertThat(dbApplication.getId()).isNotNull();
        assertThat(dbApplication.getName()).isEqualTo(application.getName());
        assertThat(dbApplication.getEmail()).isEqualTo(application.getEmail());
    }

    @Test
    public void createDuplicate() {
        // Create an application
        Application application = Application.builder().name("Application_1").email("app1@appcompany.com").build();
        applicationService.create(application);

        // Create another application with same name
        Application anotherApplication = Application.builder().name("Application_1").email("app12@appcompany.com").build();
        assertThatExceptionOfType(ApplicationExistsException.class).isThrownBy(() -> applicationService.create(anotherApplication));
    }

    @Test
    public void createMultipleWithSameEmail() {
        // Create an application
        Application application1 = Application.builder().name("Application_1").email("app1@appcompany.com").build();
        applicationService.create(application1);

        // Create another application with same name but different email
        Application application2 = Application.builder().name("Application_2").email("app1@appcompany.com").build();
        applicationService.create(application2);

        assertThat(applicationRepository.count()).isEqualTo(2);
    }
}
