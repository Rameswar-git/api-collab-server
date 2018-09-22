package io.apicollab.server.service;

import io.apicollab.server.constant.ApiStatus;
import io.apicollab.server.domain.Api;
import io.apicollab.server.domain.Application;
import io.apicollab.server.exception.ApiExistsException;
import io.apicollab.server.exception.NotFoundException;
import io.apicollab.server.repository.ApiRepository;
import io.apicollab.server.repository.ApiSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

@Service
public class ApiService {

    @Autowired
    private ApiRepository apiRepository;
    @Autowired
    private ApiSearchRepository searchRepository;

    List<ApiStatus> searchableStatusCodes = asList(ApiStatus.BETA, ApiStatus.STABLE, ApiStatus.LIVE, ApiStatus.DEPRECATED);
    
    @Transactional
    public Api create(Application application, Api api) {
        Optional<Api> dbApiHolder = apiRepository.findByApplicationIdAndVersion(application.getId(), api.getVersion());
        dbApiHolder.ifPresent(dbApi -> {
            throw new ApiExistsException(dbApi.getApplication().getName(), dbApi.getName(), dbApi.getVersion());
        });
        api.setApplication(application);
        return apiRepository.save(api);
    }

    @Transactional
    public void update(String apiId, Api api) {
        Api dbApi = findOne(apiId);
        dbApi.setStatus(api.getStatus());
        apiRepository.save(dbApi);
    }

    public Api findOne(String id) {
        Optional<Api> dbApiHolder = apiRepository.findById(id);
        return dbApiHolder.orElseThrow(NotFoundException::new);
    }

    public Collection<Api> findByApplication(String applicationId) {
        return apiRepository.findByApplicationId(applicationId);
    }

    public Collection<Api> getAll() {
        return apiRepository.findAllByStatusIn(searchableStatusCodes);
    }

    public Collection<Api> search(String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return Collections.emptyList();
        }
        return searchRepository.search(searchQuery, searchableStatusCodes);
    }

    public void delete(String id) {
        if (!apiRepository.existsById(id)) {
            throw new NotFoundException();
        }
        apiRepository.deleteById(id);
    }

}
