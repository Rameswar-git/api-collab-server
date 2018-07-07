package io.apicollab.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.apicollab.server.dto.ApplicationDTO;
import io.apicollab.server.repository.ApplicationRepository;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationRepository applicationRepository;

    private String validAPISpec;

    @Before
    public void cleanup() {
        validAPISpec = getFile("apis/valid.yml");
        applicationRepository.deleteAll();
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
    public void createApplicationWithValidData() throws Exception {
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name("Application 1").email("app1@applications.com").build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    public void createApplicationWithInvalidEmail() throws Exception {
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name("Application 1").email("app1.com").build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", is("email")));
    }

    @Test
    public void createApplicationWithNullNameAndEmail() throws Exception {
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name(null).email(null).build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors.*", hasSize(2)))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", isIn(Arrays.asList("name", "email"))));
    }

    @Test
    public void createApplicationWithNoName() throws Exception {
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name(null).email("app1@applications.com").build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors.*", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", is("name")));
    }

    @Test
    public void createApplicationWithNoNameAndInvalidEmail() throws Exception {
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name(null).email("applications.com").build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.validationErrors.*", hasSize(2)))
                .andExpect(jsonPath("$.validationErrors[0].fieldName", isIn(Arrays.asList("name", "email"))));
    }

    @Test
    public void createApplicationsWithDuplicateName() throws Exception {
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name("Application 1").email("app1@applications.com").build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isCreated());
        ApplicationDTO anotherApplicationDTO = ApplicationDTO.builder().name("Application 1").email("app2@applications.com").build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(anotherApplicationDTO)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void createApplicationsWithDuplicateEmail() throws Exception {
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name("Application 1").email("app1@applications.com").build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isCreated());
        ApplicationDTO anotherApplicationDTO = ApplicationDTO.builder().name("Application 2").email("app1@applications.com").build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(anotherApplicationDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    public void retrieveSingleApplication() throws Exception {
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name("Application 1").email("app1@applications.com").build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isCreated());
        mockMvc.perform((get("/applications")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications.*", hasSize(1)))
                .andExpect(jsonPath("$.applications[0].name", is(applicationDTO.getName())))
                .andExpect(jsonPath("$.applications[0].email", is(applicationDTO.getEmail())));
    }

    @Test
    public void retrieveAllApplications() throws Exception {
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name("Application 1").email("app1@applications.com").build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isCreated());
        applicationDTO = ApplicationDTO.builder().name("Application 2").email("app2@applications.com").build();
        mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform((get("/applications")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications.*", hasSize(2)))
                .andExpect(jsonPath("$.applications[0].name", isIn(Arrays.asList("Application 1", "Application 2"))))
                .andExpect(jsonPath("$.applications[0].email", isIn(Arrays.asList("app1@applications.com", "app2@applications.com"))));
    }

    @Test
    public void retrieveApplicationWithValidId() throws Exception {
        List<String> applicationIds = new ArrayList<>();
        // Create an application
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name("Application 1").email("app1@applications.com").build();
        MvcResult result1 = mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andReturn();
        assertThat(result1.getResponse().getStatus()).isEqualTo(201);
        String content = result1.getResponse().getContentAsString();
        DocumentContext documentContext = JsonPath.parse(content);
        applicationIds.add(documentContext.read("$.id"));
        // Create another application
        applicationDTO = ApplicationDTO.builder().name("Application 2").email("app2@applications.com").build();
        MvcResult result2 = mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andReturn();
        assertThat(result2.getResponse().getStatus()).isEqualTo(201);
        content = result2.getResponse().getContentAsString();
        documentContext = JsonPath.parse(content);
        applicationIds.add(documentContext.read("$.id"));

        mockMvc.perform((get("/applications/" + applicationIds.get(0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(applicationIds.get(0))));

        mockMvc.perform((get("/applications/" + applicationIds.get(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(applicationIds.get(1))));
    }

    @Test
    public void retrieveApplicationWithInvalidId() throws Exception {
        mockMvc.perform((get("/applications/someinvalidid")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateExistingApplication() throws Exception {
        // Create an application
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name("Application 1").email("app1@applications.com").build();
        MvcResult result1 = mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andReturn();
        assertThat(result1.getResponse().getStatus()).isEqualTo(201);
        String content = result1.getResponse().getContentAsString();
        DocumentContext documentContext = JsonPath.parse(content);
        String applicationId = documentContext.read("$.id");

        // Update
        applicationDTO = ApplicationDTO.builder().name("Application 1 Updated").email("app1Updated@applications.com").build();
        mockMvc.perform((put("/applications/" + applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO))))
                .andExpect(status().isOk());

        //Retrieve and validate
        mockMvc.perform((get("/applications/" + applicationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(applicationId)))
                .andExpect(jsonPath("$.name", is(applicationDTO.getName())))
                .andExpect(jsonPath("$.email", is(applicationDTO.getEmail())));
    }

    @Test
    public void updateInvalidApplication() throws Exception {
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name("Application 1").email("app1@applications.com").build();
        // Update
        mockMvc.perform((put("/applications/someinvalidid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO))))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteNonExistingApplication() throws Exception {
        mockMvc.perform(delete("/applications/12345")).andExpect(status().isNotFound());
    }

    @Test
    public void deleteApplication() throws Exception {
        // Create
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name("Application 1").email("app1@applications.com").build();
        MvcResult mvcResult = mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        // Retrieve
        mockMvc.perform((get("/applications/" + jsonNode.get("id").asText())))
                .andExpect(status().isOk());
        // Delete
        mockMvc.perform(delete("/applications/" + jsonNode.get("id").asText()))
                .andExpect(status().isNoContent());
        // Confirm
        mockMvc.perform((get("/applications/" + jsonNode.get("id").asText())))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteApplicationWithApis() throws Exception {
        // Create
        ApplicationDTO applicationDTO = ApplicationDTO.builder().name("Application 1").email("app1@applications.com").build();
        MvcResult mvcResult = mockMvc.perform(post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        // Create Api
        String spec = validAPISpec;
        MockMultipartFile swaggerDoc = new MockMultipartFile("swaggerDoc", spec.getBytes());
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .multipart("/applications/"+jsonNode.get("id").asText()+"/apis")
                .file(swaggerDoc);
        mockMvc.perform(builder).andExpect(status().isCreated());
        // Delete
        mockMvc.perform(delete("/applications/" + jsonNode.get("id").asText()))
                .andExpect(status().isConflict());
    }
}
