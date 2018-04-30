package io.apicollab.server.controller;

import io.apicollab.server.domain.Api;
import io.apicollab.server.dto.ApiDTO;
import io.apicollab.server.dto.ApiListDTO;
import io.apicollab.server.exception.ApiPortalException;
import io.apicollab.server.mapper.ApiMapper;
import io.apicollab.server.service.ApiService;
import io.apicollab.server.service.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@RestController
@Validated
@Slf4j
public class ApiController {

    @Autowired
    private ApiService apiService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApiMapper apiMapper;

    @GetMapping("/applications/{applicationId}/apis")
    public ApiListDTO getApplicationApis(@PathVariable String applicationId) {
        applicationService.findById(applicationId);
        return ApiListDTO.builder().apis(apiMapper.toDtos(apiService.findByApplication(applicationId))).build();
    }

    @GetMapping("/apis/{apiId}")
    public ApiDTO getApplicationApi(@PathVariable String apiId) {
        return apiMapper.toDto(apiService.findOne(apiId));
    }

    @PostMapping(value = "/applications/{applicationId}/apis", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDTO create(@PathVariable String applicationId,
                         @Valid ApiDTO apiDTO,
                         @RequestPart("swaggerDoc") final MultipartFile swaggerDoc) {
        Api api = apiMapper.toEntity(apiDTO);
        api.setSwaggerDefinition(extractFileContent(swaggerDoc));
        return apiMapper.toDto(applicationService.createNewApiVersion(applicationId, api));
    }

    @GetMapping("/apis/{apiId}/swaggerDoc")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Resource> getApiDefinition(@PathVariable String apiId) {
        Api api = apiService.findOne(apiId);
        return ResponseEntity.ok()
                .contentLength(api.getSwaggerDefinition().length())
                .body(new ByteArrayResource(api.getSwaggerDefinition().getBytes()));
    }

    private String extractFileContent(final MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder swaggerDefinitionContent = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    swaggerDefinitionContent.append(line);
                }
                return swaggerDefinitionContent.toString();
            }
        } catch (IOException e) {
            throw new ApiPortalException("Error occurred while processing swagger document");
        }
    }
}
