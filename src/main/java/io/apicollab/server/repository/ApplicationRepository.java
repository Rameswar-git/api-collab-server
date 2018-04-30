package io.apicollab.server.repository;

import io.apicollab.server.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, String> {

    Optional<Application> findByName(String name);
}
