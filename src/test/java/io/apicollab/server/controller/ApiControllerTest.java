package io.apicollab.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.apicollab.server.repository.ApiRepository;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/before.sql")
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/after.sql")
public class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiRepository apiRepository;

    private String validAPISpec;

    @Before
    public void cleanup() {
        validAPISpec = getFile("apis/valid.yml");
        apiRepository.deleteAll();
    }

    private String getFile(String fileName) {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Test
    public void createApiWithNoNameAndNoVersion() throws Exception {
        String spec = validAPISpec
                .replaceFirst("title:.*", "title:")
                .replaceFirst("version:.*", "version:");
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        mockMvc.perform(builder).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors.*", hasSize(2)))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", isIn(Arrays.asList("name", "version"))));
    }

    @Test
    public void createApiWithNoName() throws Exception {
        String spec = validAPISpec
                .replaceFirst("title:.*", "title:");
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        mockMvc.perform(builder).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors.*", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", is("name")));
    }

    @Test
    public void createApiWithNoVersion() throws Exception {
        String spec = validAPISpec
                .replaceFirst("version:.*", "version:");
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        mockMvc.perform(builder).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors.*", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", is("version")));
    }

    @Test
    public void createApiWithNoSwaggerDoc() throws Exception {
        String spec = "";
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        mockMvc.perform(builder).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors.*", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", is("swaggerDoc")));
    }

    @Test
    public void createApiWithValidData() throws Exception {
        String spec = validAPISpec;
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        mockMvc.perform(builder).andExpect(status().isCreated());
    }

    @Test
    public void createDuplicateApi() throws Exception {
        String spec = validAPISpec;
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        mockMvc.perform(builder).andExpect(status().isCreated());
        mockMvc.perform(builder).andExpect(status().isConflict());
    }

    @Test
    public void updateApiStatus() throws Exception {
        // Create
        String spec = validAPISpec;
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        MvcResult mvcResult = mockMvc.perform(builder).andExpect(status().isCreated()).andReturn();
        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        // Update
        mockMvc.perform(put("/apis/" + jsonNode.get("id").asText())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"status\":\"STABLE\"}"))
                .andExpect(status().isNoContent());
        // Verify
        mockMvc.perform(get("/apis/" + jsonNode.get("id").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("STABLE")))
                .andExpect(jsonPath("$.name", is(jsonNode.get("name").asText())))
                .andExpect(jsonPath("$.version", is(jsonNode.get("version").asText())))
                .andExpect(jsonPath("$.description", is(jsonNode.get("description").asText())))
                .andExpect(jsonPath("$.tags.*", hasSize(1)));
    }

    @Test
    public void updateApiWithInvalidStatus() throws Exception {
        // Create
        String spec = validAPISpec;
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        MvcResult mvcResult = mockMvc.perform(builder).andExpect(status().isCreated()).andReturn();
        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        // Update
        mockMvc.perform(put("/apis/" + jsonNode.get("id").asText())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"status\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateApiWithNoBody() throws Exception {
        // Create
        String spec = validAPISpec;
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        MvcResult mvcResult = mockMvc.perform(builder).andExpect(status().isCreated()).andReturn();
        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        // Update
        mockMvc.perform(put("/apis/" + jsonNode.get("id").asText()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateNonExistingApi() throws Exception {
        mockMvc.perform(put("/apis/12345")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"status\":\"STABLE\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getSwaggerDocument() throws Exception {
        // Create API
        String spec = validAPISpec;
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        MvcResult postResult = mockMvc.perform(builder).andReturn();
        assertThat(postResult.getResponse().getStatus()).isEqualTo(201);
        DocumentContext documentContext = JsonPath.parse(postResult.getResponse().getContentAsString());
        String apiId = documentContext.read("$.id");

        // Fetch Swagger doc
        MvcResult getResult = mockMvc.perform(get("/apis/" + apiId + "/swaggerDoc")).andReturn();
        assertThat(getResult.getResponse().getContentAsString()).isEqualTo(new String(swaggerDoc.getBytes(), "UTF-8"));
    }

    @Test
    public void getSwaggerDocumentForNonExistingApi() throws Exception {
        mockMvc.perform(get("/apis/5/swaggerDoc")).andExpect(status().isNotFound());
    }

    @Test
    public void listApplicationApis() throws Exception {
        String spec1v1 = validAPISpec
                .replaceFirst("title:.*", "title: A")
                .replaceFirst("version.*", "version: 1.0");

        String spec1v2 = validAPISpec
                .replaceFirst("title:.*", "title: A")
                .replaceFirst("version.*", "version: 2.0");

        String spec2v1 = validAPISpec
                .replaceFirst("title:.*", "title: B")
                .replaceFirst("version.*", "version: 3.0");

        MockMultipartFile swaggerDoc1v1 = new MockMultipartFile("swaggerDoc", spec1v1.getBytes());
        MockMultipartFile swaggerDoc1v2 = new MockMultipartFile("swaggerDoc", spec1v2.getBytes());
        MockMultipartFile swaggerDoc2v1 = new MockMultipartFile("swaggerDoc", spec2v1.getBytes());

        // Create A.V1
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc1v1);
        mockMvc.perform(builder).andExpect(status().isCreated());

        // Create A.V2
        builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc1v2);
        mockMvc.perform(builder).andExpect(status().isCreated());

        // Create B.V1
        builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc2v1);
        mockMvc.perform(builder).andExpect(status().isCreated());

        mockMvc.perform(get("/applications/1/apis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(3))
                .andExpect(jsonPath("$.items.*", hasSize(3)));
    }

    @Test
    public void listAllApis() throws Exception {
        String spec1v1 = validAPISpec
                .replaceFirst("title:.*", "title: Fruits API")
                .replaceFirst("version.*", "version: 1.0")
                .replaceFirst("description.*", "description: Banana apples oranges are cool");

        String spec2v1 = validAPISpec
                .replaceFirst("title:.*", "title: Space API")
                .replaceFirst("version.*", "version: 2.0")
                .replaceFirst("description.*", "description: Space time planets rockets");

        MockMultipartFile swaggerDoc1v1 = new MockMultipartFile("swaggerDoc", spec1v1.getBytes());
        MockMultipartFile swaggerDoc2v1 = new MockMultipartFile("swaggerDoc", spec2v1.getBytes());

        // Create A.V1
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc1v1);
        mockMvc.perform(builder).andExpect(status().isCreated());

        // Create A.V2
        builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc2v1);
        mockMvc.perform(builder).andExpect(status().isCreated());

        // Fetch All
        mockMvc.perform(get("/apis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.items.*", hasSize(2)));

    }

    @Test
    public void searchApis() throws Exception {
        String spec1v1 = validAPISpec
                .replaceFirst("title:.*", "title: Fruits API")
                .replaceFirst("version.*", "version: 1.0")
                .replaceFirst("description.*", "description: Banana apples oranges are cool");

        String spec2v1 = validAPISpec
                .replaceFirst("title:.*", "title: Space API")
                .replaceFirst("version.*", "version: 2.0")
                .replaceFirst("description.*", "description: Space time planets rockets");

        MockMultipartFile swaggerDoc1v1 = new MockMultipartFile("swaggerDoc", spec1v1.getBytes());
        MockMultipartFile swaggerDoc2v1 = new MockMultipartFile("swaggerDoc", spec2v1.getBytes());

        // Create A.V1
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc1v1);
        mockMvc.perform(builder).andExpect(status().isCreated());

        // Create A.V2
        builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc2v1);
        mockMvc.perform(builder).andExpect(status().isCreated());

        // Fetch All
        mockMvc.perform(get("/apis/search?query=apples"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.items.*", hasSize(1)))
                .andExpect(jsonPath("$.items.[0].name").value("Fruits API"));


        mockMvc.perform(get("/apis/search?query=space"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.items.*", hasSize(1)))
                .andExpect(jsonPath("$.items.[0].name").value("Space API"));
    }

    @Test
    public void deleteNonExistingApi() throws Exception {
        mockMvc.perform(delete("/apis/12345")).andExpect(status().isNotFound());
    }

    @Test
    public void deleteApi() throws Exception {
        // Create
        String spec = validAPISpec;
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        MvcResult mvcResult = mockMvc.perform(builder).andExpect(status().isCreated()).andReturn();
        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        // Retrieve
        mockMvc.perform(get("/apis/" + jsonNode.get("id").asText())).andExpect(status().isOk());
        // Delete
        mockMvc.perform(delete("/apis/" + jsonNode.get("id").asText())).andExpect(status().isNoContent());
        // Confirm
        mockMvc.perform(get("/apis/" + jsonNode.get("id").asText())).andExpect(status().isNotFound());
    }
}
