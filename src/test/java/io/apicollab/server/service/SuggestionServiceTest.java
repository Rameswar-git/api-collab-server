package io.apicollab.server.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SuggestionServiceTest {

    private static final String TEXT_1 = "This is doc1";
    private static final String TEXT_2 = "This is doc2";
    private static final String JSON_STRING = "{\"name\":\"Steve\", \"startDate\":\" The start Date\" }";

    @Autowired
    SuggestionService service;

    @Test
    public void testIndexing() {
        service.processDocuments(asList(TEXT_1, TEXT_2));
    }

    @Test
    public void testSearchFullWord() {
        service.processDocuments(asList(TEXT_1, TEXT_2));
        String query = "doc1";
        List<String> words = service.search(query);
        assertThat(words).hasSize(2);
        assertThat(words.get(0)).isEqualToIgnoringCase(query);

    }

    @Test
    public void testSearchPartialWordStart() {
        service.processDocuments(asList(TEXT_1, TEXT_2));
        String query = "doc";
        List<String> words = service.search(query);
        assertThat(words).hasSize(2);
        assertThat(words).allMatch( w -> w.startsWith(query));
    }

    @Test
    public void testSearchPartialWordEnd() {
        service.processDocuments(asList(TEXT_1, TEXT_2));
        String query = "oc1";
        List<String> words = service.search(query);
        assertThat(words.size()).isGreaterThan(1);
    }

    @Test
    public void testMiddleWord() {
        service.processDocuments(asList(JSON_STRING));
        String query = "rt";
        List<String> words = service.search(query);
        assertThat(words).contains("startDate", "start");
    }
}
