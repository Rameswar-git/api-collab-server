package io.apicollab.server.repository;

import io.apicollab.server.constant.ApiStatus;
import io.apicollab.server.domain.Api;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@Transactional
public class ApiSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Api> search(String searchString, List<ApiStatus> statusCodes) {
        FullTextEntityManager fullTextEntityManager
                = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Api.class)
                .get();


        // Select status codes
        Query statusQuery = queryBuilder
                .simpleQueryString()
                .onField("status")
                .matching(StringUtils.join(" ", statusCodes))
                .createQuery();

        Query searchQuery = queryBuilder.simpleQueryString()
                .onFields("swaggerDefinition")
                .matching(searchString)
                .createQuery();


        // Combine query
        Query query = queryBuilder
                .bool()
                .must(statusQuery)
                .must(searchQuery)
                .createQuery();

        org.hibernate.search.jpa.FullTextQuery jpaQuery
                = fullTextEntityManager.createFullTextQuery(query, Api.class);

        List<Api> results = jpaQuery.getResultList();
        fullTextEntityManager.close();

        return results;
    }

}
