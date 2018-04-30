package io.apicollab.server.service;

import io.apicollab.server.domain.Api;
import io.apicollab.server.domain.Application;
import io.apicollab.server.exception.ApiExistsException;
import io.apicollab.server.exception.NotFoundException;
import io.apicollab.server.repository.ApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
public class ApiService {

    @Autowired
    private ApiRepository apiRepository;

    @Transactional
    public Api create(Application application, Api api) {
        Optional<Api> dbApiHolder = apiRepository.findByApplicationIdAndNameAndVersion(application.getId(), api.getName(), api.getVersion());
        dbApiHolder.ifPresent(dbApi -> {
            throw new ApiExistsException(dbApi.getApplication().getName(), dbApi.getName(), dbApi.getVersion());
        });
        api.setApplication(application);
        return apiRepository.save(api);
    }

    public Api findOne(String id) {
        Optional<Api> dbApiHolder = apiRepository.findOne(Example.of(Api.builder().id(id).build()));
        return dbApiHolder.orElseThrow(NotFoundException::new);
    }

    public Collection<Api> findByApplication(String applicationId) {
        return apiRepository.findByApplicationId(applicationId);
    }
}
