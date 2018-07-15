package io.apicollab.server.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
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
    private static RAMDirectory directory = null;

    @Autowired
    private ApiService apiService;

    /**
     * Periodically refresh the keywords based on the api specifications
     */
    @Scheduled(fixedDelayString = "${api-suggestions-refresh-rate-milliseconds}")
    public void processAllApiDocuments(){
        log.debug("Refreshing Api suggestions");
        List<String> specs = apiService.getAll().stream().map(api-> api.getSwaggerDefinition()).collect(Collectors.toList());
        processDocuments(specs);
        log.debug("Refreshing Api suggestions complete");
    }

    /**
     * Peforms fuzzy search and wildcard search for a given word
     * @param partialKeyword
     * @return The matching suggestions
     */
    public List<String> search(String partialKeyword){
        List<String> results = new ArrayList<>();
        String wildCardKeyword = String.format("*%s*",partialKeyword);
        try
        {
            //Create Reader
            DirectoryReader reader = DirectoryReader.open(directory);
            //Create index searcher
            IndexSearcher searcher = new IndexSearcher(reader);
            //Build query
            Query fuzzyQuery = new FuzzyQuery(new Term(INDEX_FIELD_NAME, partialKeyword));
            StandardQueryParser queryParserHelper = new StandardQueryParser();
            queryParserHelper.setAllowLeadingWildcard(true);
            Query query = null;
            try {
                query = queryParserHelper.parse(wildCardKeyword, INDEX_FIELD_NAME);
            } catch (QueryNodeException e) {
                log.error("Failed to build wildcard query for input {}", wildCardKeyword, e);
            }
            if(query == null){
                // resort to PrefixQuery query
                query = new PrefixQuery(new Term(INDEX_FIELD_NAME, partialKeyword));
            }
            BooleanQuery combinedQuery = new BooleanQuery.Builder()
                    .add(fuzzyQuery, BooleanClause.Occur.SHOULD)
                    .add(query,BooleanClause.Occur.SHOULD)
                    .build();

            // Search
            TopDocs foundDocs = searcher.search(combinedQuery, 10);
            // Total found documents
            log.debug("Total Results :: " + foundDocs.totalHits);
            for (ScoreDoc sd : foundDocs.scoreDocs)
            {
                Document d = searcher.doc(sd.doc);
                results.add(d.get(INDEX_FIELD_NAME));
            }
            reader.close();
        }
        catch (IOException e)
        {
            // any error goes here
            log.error("Failed to search", e);
        }
        return results;
    }

    /**
     * Index the documents
     * Builds a set of keywords from documents and adds to indexs
     * @param documents
     */
    public void processDocuments(List<String> documents){
        // Build a set of suggestions.
        Set<String> suggestions = new HashSet<>();
        for (String document: documents) {
            List<String> terms = asList(document.split(REGEX_INVALID_CHAR));
            terms = terms.stream().filter(s-> !s.isEmpty()).collect(Collectors.toList());
            suggestions.addAll(terms);
        }
        processSuggestions(suggestions);
    }


    /**
     * Process each keyword by adding the search index
     * @param suggestions
     */
    private void processSuggestions(Set<String> suggestions){

        log.debug("Starting to index {} words", suggestions.size());
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        RAMDirectory newDirectory = new RAMDirectory();
        // IndexWriter writes new index files to the directory
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(newDirectory, config);
            for (String suggestion : suggestions) {
                indexSuggestion(writer, suggestion);
            }
            log.debug("Completed indexing {} words", suggestions.size());
            writer.close();
            directory = newDirectory;
        } catch (IOException e) {
            log.error("Failed indexing {} words", suggestions.size(), e);
        }
    }

    /**
     * Helper method to add a single document to the index
     * @param writer
     * @param suggestion
     * @throws IOException
     */
    static void indexSuggestion(IndexWriter writer, String suggestion) throws IOException
    {
        Document doc = new Document();
        doc.add(new TextField(INDEX_FIELD_NAME, suggestion, Field.Store.YES));
        writer.addDocument(doc);
    }

}
