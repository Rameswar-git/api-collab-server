package io.apicollab.server.repository;

import io.apicollab.server.domain.Api;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ApiRepository extends JpaRepository<Api, String> {

    Optional<Api> findByApplicationIdAndNameAndVersion(String applicationId, String name, String version);

    Collection<Api> findByApplicationId(String applicationId);
}
