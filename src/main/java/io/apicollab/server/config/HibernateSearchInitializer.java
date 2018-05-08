package io.apicollab.server.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
@Slf4j
public class HibernateSearchInitializer implements ApplicationListener<ApplicationReadyEvent> {
    @PersistenceContext
    EntityManager entityManager;

    /**
     * Create an initial Lucene index for the data already present in the
     * database.
     * This method is called when Spring's startup.
     */
    @Override
    @Transactional
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        try {
            FullTextEntityManager fullTextEntityManager =
                    Search.getFullTextEntityManager(entityManager);
            fullTextEntityManager.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            log.error("Failed to index search engine", e);
        }
        return;
    }
}
