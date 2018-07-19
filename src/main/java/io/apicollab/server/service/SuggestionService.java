package io.apicollab.server.service;

import io.apicollab.server.domain.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Slf4j
@Service
public class SuggestionService {

    private static final String INDEX_FIELD_NAME = "suggestionId";
    private static final String REGEX_INVALID_CHAR = "[\\s@\"&:{}/#.,?$+-]+";
    private static final int FUZZY_MIN_WORD_LENGTH = 4;

    private RAMDirectory directory = new RAMDirectory();
    private IndexWriter indexWriter;
    private SearcherManager searcherManager;

    @Autowired
    private ApiService apiService;

    @PostConstruct
    protected void initialize() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        indexWriter = new IndexWriter(directory, config);
        searcherManager = new SearcherManager(indexWriter, null);
    }

    /**
     * Periodically refresh the keywords based on the api specifications
     */
    @Scheduled(fixedDelayString = "${api-suggestions-refresh-rate-milliseconds}")
    public void processAllApiDocuments() {
        log.debug("Refreshing Api suggestions");
        List<String> specs = apiService.getAll().stream().map(Api::getSwaggerDefinition).collect(Collectors.toList());
        processDocuments(specs);
        log.debug("Refreshing Api suggestions complete");
    }

    /**
     * Performs fuzzy search and wildcard search for a given word
     *
     * @param partialKeyword
     * @return The matching suggestions
     */
    public List<String> search(String partialKeyword) {
        List<String> results = new ArrayList<>();
        try {
            IndexSearcher searcher = searcherManager.acquire();
            // Build Query
            Query combinedQuery = constructQueries(partialKeyword);
            // Perform Search
            TopDocs foundDocs = searcher.search(combinedQuery, 10);
            // Total found documents
            log.debug("Total Results :: ", foundDocs.totalHits);
            for (ScoreDoc sd : foundDocs.scoreDocs) {
                Document d = searcher.doc(sd.doc);
                results.add(d.get(INDEX_FIELD_NAME));
            }
            searcherManager.release(searcher);
        } catch (IOException e) {
            // any error goes here
            log.error("Failed to search", e);
        }
        return results;
    }

    /**
     * Builds a set of queries and combines in to a single boolean OR query
     */
    private Query constructQueries(String word) {
        //Build multiple queries
        List<Query> queries = new ArrayList<>();
        // Fuzzy if applicable
        appendFuzzyQuery(word, queries);
        // Wildcard query if possible
        appendWildcardQuery(word, queries);
        // Combine all queries
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        queries.forEach(q -> builder.add(q, BooleanClause.Occur.SHOULD));
        return builder.build();
    }

    private void appendFuzzyQuery(String word, List<Query> queries) {
        if (word.length() >= FUZZY_MIN_WORD_LENGTH) {
            queries.add(new FuzzyQuery(new Term(INDEX_FIELD_NAME, word)));
        }
    }

    /**
     * Appends a wild card query, otherwise fallbacks to prefix query
     */
    private void appendWildcardQuery(String word, List<Query> queries) {
        StandardQueryParser queryParserHelper = new StandardQueryParser();
        queryParserHelper.setAllowLeadingWildcard(true);
        Query query = null;
        String wildCardKeyword = String.format("*%s*", word.replace("*", ""));
        try {
            query = queryParserHelper.parse(wildCardKeyword, INDEX_FIELD_NAME);
        } catch (QueryNodeException e) {
            log.error("Failed to build wildcard query for input {}", wildCardKeyword, e);
            // fallback to prefix query
            query = new PrefixQuery(new Term(INDEX_FIELD_NAME, word));
        }
        queries.add(query);
    }

    /**
     * Index the documents
     * Builds a set of keywords from documents and adds to indexs
     *
     * @param documents
     */
    public void processDocuments(List<String> documents) {
        // Build a set of suggestions.
        Set<String> suggestions = new HashSet<>();
        for (String document : documents) {
            List<String> terms = asList(document.split(REGEX_INVALID_CHAR));
            terms = terms.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
            suggestions.addAll(terms);
        }
        processSuggestions(suggestions);
        try {
            indexWriter.commit();
            searcherManager.maybeRefresh();
            log.debug("Index refreshed");
        } catch (IOException e) {
            log.error("Index refresh failed", e);
        }
    }


    /**
     * Process each keyword by adding the search index
     *
     * @param suggestions
     */
    private void processSuggestions(Set<String> suggestions) {
        log.debug("Starting to index {} words", suggestions.size());
        try {
            for (String suggestion : suggestions) {
                indexSuggestion(indexWriter, suggestion);
            }
            log.debug("Completed indexing {} words", suggestions.size());
        } catch (IOException e) {
            log.error("Failed indexing {} words", suggestions.size(), e);
        }
    }

    /**
     * Helper method to add a single document to the index
     *
     * @param writer
     * @param suggestion
     * @throws IOException
     */
    static void indexSuggestion(IndexWriter writer, String suggestion) throws IOException {
        Document doc = new Document();
        doc.add(new TextField(INDEX_FIELD_NAME, suggestion, Field.Store.YES));
        writer.updateDocument(new Term(INDEX_FIELD_NAME, suggestion), doc);
    }

}
