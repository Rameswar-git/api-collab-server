package io.apicollab.server.service;

import io.apicollab.server.domain.Api;
import io.apicollab.server.domain.Application;
import io.apicollab.server.exception.ApplicationExistsException;
import io.apicollab.server.exception.NotFoundException;
import io.apicollab.server.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApiService apiService;

    public Collection<Application> getAll() {
        return applicationRepository.findAll();
    }

    @Transactional
    public Application create(Application application) {
        Optional<Application> dbApplicationHolder = applicationRepository.findByName(application.getName());
        dbApplicationHolder.ifPresent(dbApplication -> {
            throw new ApplicationExistsException(dbApplication.getName());
        });
        return applicationRepository.save(application);
    }

    @Transactional
    public Application save(Application application) {
        Application dbApplication = findById(application.getId());
        dbApplication.setName(application.getName());
        dbApplication.setEmail(application.getEmail());
        return applicationRepository.save(dbApplication);
    }

    @Transactional
    public Application findById(String id) {
        Optional<Application> dbApplicationHolder = applicationRepository.findById(id);
        return dbApplicationHolder.orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Api createNewApiVersion(String applicationId, Api api) {
        Application dbApplication = findById(applicationId);
        return apiService.create(dbApplication, api);
    }
}
