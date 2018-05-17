package io.apicollab.server.service;

import io.apicollab.server.dto.ApiDTO;
import io.apicollab.server.exception.ApiParsingException;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Returns metadata from a parsed API specification file
 *
 */
@UtilityClass
public class ApiSpecParserService {

    /**
     * Parses a swagger file and returns a DTO with metadata.
     * @param spec
     * @return
     */
    public static ApiDTO parse(String spec) {
        if(StringUtils.isBlank(spec)){
            throw new ApiParsingException("API specification is empty", asList("API specification is empty"));
        }
        spec = spec.trim();
        ApiDTO result = null;
        if(spec.contains("openapi:") || spec.contains("openapi\"")){
            // OAS Spec
            result =  parseOAS(spec);
        } else {
            result =  parseSwagger(spec);
        }
        return result;
    }

    private ApiDTO parseOAS(String oasString){

        SwaggerParseResult result = new OpenAPIParser().readContents(oasString, null,  null);
        if(result.getOpenAPI()== null || !result.getMessages().isEmpty()){
            throw new ApiParsingException("Failed to parse OAS file", result.getMessages());
        }else{
            OpenAPI openAPI = result.getOpenAPI();
            Info info = openAPI.getInfo();
            List<String> tags = openAPI.getTags() == null ? null :  openAPI.getTags().stream().map(io.swagger.v3.oas.models.tags.Tag::getName).collect(Collectors.toList());

            // Check basic information
            String title = info.getTitle();
            if(title == "null") title = null;

            String version = info.getVersion();
            if(version == "null") version = null;

            return ApiDTO.builder()
                    .name(title)
                    .version(version)
                    .tags(tags)
                    .description(info.getDescription())
                    .swaggerDefinition(oasString).build();
        }
    }
    private ApiDTO parseSwagger(String swaggerString){
        Swagger swagger = new SwaggerParser().parse(swaggerString);
        if(swagger == null){
            throw new IllegalArgumentException("Failed to parse swagger file");
        }else{
            io.swagger.models.Info info = swagger.getInfo();
            List<String> tags = swagger.getTags() == null ? null :  swagger.getTags().stream().map(Tag::getName).collect(Collectors.toList());
            return ApiDTO.builder()
                    .name(info.getTitle())
                    .version(info.getVersion())
                    .tags(tags)
                    .description(info.getDescription())
                    .swaggerDefinition(swaggerString).build();
        }
    }

}
