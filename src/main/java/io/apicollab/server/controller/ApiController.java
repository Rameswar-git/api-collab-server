package io.apicollab.server.controller;

import io.apicollab.server.constant.ApiStatus;
import io.apicollab.server.domain.Api;
import io.apicollab.server.dto.ApiDTO;
import io.apicollab.server.dto.ApiListDTO;
import io.apicollab.server.dto.ApiUpdateInput;
import io.apicollab.server.exception.ApiPortalException;
import io.apicollab.server.mapper.ApiMapper;
import io.apicollab.server.service.ApiService;
import io.apicollab.server.service.ApiSpecParserService;
import io.apicollab.server.service.ApplicationService;
import io.apicollab.server.web.commons.APIErrors;
import io.apicollab.server.web.commons.APIException;
import io.apicollab.server.web.commons.APIValidationException;
import io.apicollab.server.web.commons.ValidationResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

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
                         @RequestPart("swaggerDoc") final MultipartFile swaggerDoc) {
        if (swaggerDoc.isEmpty()) {
            ValidationResultDTO resultDTO = new ValidationResultDTO("swaggerDoc", "API Specification is empty", "");
            throw new APIValidationException(APIErrors.VALIDATION_ERROR, asList(resultDTO));
        }
        ApiDTO apiDTO = ApiSpecParserService.parse(extractFileContent(swaggerDoc));
        apiDTO.setStatus(ApiStatus.BETA.toString());
        validateDTO(apiDTO);
        Api api = apiMapper.toEntity(apiDTO);
        return apiMapper.toDto(applicationService.createNewApiVersion(applicationId, api));
    }

    @PutMapping(value = "/apis/{apiId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String apiId,
                         @RequestBody @Valid ApiUpdateInput apiDTO) {
        Api api = null;
        try {
            api = Api.builder().status(ApiStatus.valueOf(apiDTO.getStatus())).build();
        } catch(IllegalArgumentException ex) {
            throw new APIException("Invalid status code provided", APIErrors.VALIDATION_ERROR.toString(), APIErrors.VALIDATION_ERROR.status);
        }
        apiService.update(apiId, api);
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
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new ApiPortalException("Error occurred while processing swagger document");
        }
    }

    private void validateDTO(ApiDTO apiDTO) {
        List<ValidationResultDTO> validation = new ArrayList<>();
        if (apiDTO.getName() == null || apiDTO.getName().isEmpty()) {
            validation.add(new ValidationResultDTO("name", "Missing title/name in the API specification", apiDTO.getName()));
        }
        if (apiDTO.getVersion() == null || apiDTO.getVersion().isEmpty()) {
            validation.add(new ValidationResultDTO("version", "Missing version in the API specification", apiDTO.getVersion()));
        }
        if (apiDTO.getDescription() == null || apiDTO.getDescription().isEmpty()) {
            validation.add(new ValidationResultDTO("description", "Missing description in the API specification", apiDTO.getDescription()));
        }
        if (!validation.isEmpty()) {
            throw new APIValidationException(APIErrors.VALIDATION_ERROR, validation);
        }
    }
}
