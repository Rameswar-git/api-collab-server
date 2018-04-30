package io.apicollab.server.controller;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.apicollab.server.repository.ApiRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private ApiRepository apiRepository;

    @Before
    public void cleanup() {
        apiRepository.deleteAll();
    }

    @Test
    public void createApiWithNoNameAndNoVersion() throws Exception {
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", "{\"openapi\":\"3.0.0\"}".getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc);
        mockMvc.perform(builder).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors.*", hasSize(2)))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", isIn(Arrays.asList("name", "version"))));
    }

    @Test
    public void createApiWithNoName() throws Exception {
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", "{\"openapi\":\"3.0.0\"}".getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc)
                .param("version", "0.1");
        mockMvc.perform(builder).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors.*", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", is("name")));
    }

    @Test
    public void createApiWithNoVersion() throws Exception {
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", "{\"openapi\":\"3.0.0\"}".getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc)
                .param("name", "API1");
        mockMvc.perform(builder).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors.*", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", is("version")));
    }

    @Test
    public void createApiWithNoSwaggerDoc() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .param("name", "API1")
                .param("version", "0.1");
        mockMvc.perform(builder).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors.*", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", is("swaggerDoc")));
    }

    @Test
    public void createApiWithValidData() throws Exception {
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", "{\"openapi\":\"3.0.0\"}".getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc)
                .param("name", "API1")
                .param("version", "0.1");
        mockMvc.perform(builder).andExpect(status().isCreated());
    }

    @Test
    public void createDuplicateApi() throws Exception {
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", "{\"openapi\":\"3.0.0\"}".getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc)
                .param("name", "API1")
                .param("version", "0.1");
        mockMvc.perform(builder).andExpect(status().isCreated());
        mockMvc.perform(builder).andExpect(status().isConflict());
    }

    @Test
    public void getSwaggerDocument() throws Exception {
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", "{\"openapi\":\"3.0.0\"}".getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc)
                .param("name", "API1")
                .param("version", "0.1");
        MvcResult postResult = mockMvc.perform(builder).andReturn();
        assertThat(postResult.getResponse().getStatus()).isEqualTo(201);
        DocumentContext documentContext = JsonPath.parse(postResult.getResponse().getContentAsString());
        String apiId = documentContext.read("$.id");

        MvcResult getResult = mockMvc.perform(get("/apis/" + apiId + "/swaggerDoc")).andReturn();
        assertThat(getResult.getResponse().getContentAsString()).isEqualTo(new String(swaggerDoc.getBytes(), "UTF-8"));
    }

    @Test
    public void getSwaggerDocumentForNonExistingApi() throws Exception {
        mockMvc.perform(get("/apis/5/swaggerDoc")).andExpect(status().isNotFound());
    }

    @Test
    public void listApplicationApis() throws Exception {
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", "{\"openapi\":\"3.0.0\"}".getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc)
                .param("name", "API1")
                .param("version", "0.1");
        mockMvc.perform(builder).andExpect(status().isCreated());
        builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc)
                .param("name", "API1")
                .param("version", "0.2");
        mockMvc.perform(builder).andExpect(status().isCreated());
        builder = MockMvcRequestBuilders.multipart("/applications/1/apis")
                .file(swaggerDoc)
                .param("name", "API2")
                .param("version", "0.1");
        mockMvc.perform(builder).andExpect(status().isCreated());

        mockMvc.perform(get("/applications/1/apis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apis.*", hasSize(3)));
    }
}
