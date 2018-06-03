package io.apicollab.server.service;

import io.apicollab.server.dto.ApiDTO;
import io.apicollab.server.exception.ApiParsingException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ApiSpecParserServiceTest {

    private String validSpec;
    private String invalidSpec;
    private String incompleteSpec;

    @Before
    public void setup() throws IOException {
        validSpec = getFile("apis/valid.yml");
        invalidSpec =  getFile("apis/invalid.yml");
        incompleteSpec = getFile("apis/incomplete.yml");
    }

    @Test
    public void emptySpecThrowsApiParsingException() {
        assertThatExceptionOfType(ApiParsingException.class).isThrownBy(() -> {
           ApiSpecParserService.parse("");
        });
    }

    @Test
    public void blankSpecThrowsApiParsingException() {
        assertThatExceptionOfType(ApiParsingException.class).isThrownBy(() -> {
            ApiSpecParserService.parse("");
        });
    }

    @Test
    public void parseValid(){
        ApiDTO dto = ApiSpecParserService.parse(validSpec);
        assertThat(dto.getName()).isNotBlank();
        assertThat(dto.getVersion()).isNotBlank();
        assertThat(dto.getDescription()).isNotBlank();
        assertThat(dto.getTags()).isNotEmpty();
        assertThat(dto.getSwaggerDefinition()).isNotBlank();
    }

    @Test
    public void parseValidSwagger(){
        String validSwaggerSpec = getFile("apis/valid_swagger.yml");
        ApiDTO dto = ApiSpecParserService.parse(validSwaggerSpec);
        assertThat(dto.getName()).isNotBlank();
        assertThat(dto.getVersion()).isNotBlank();
        assertThat(dto.getDescription()).isNotBlank();
        assertThat(dto.getTags()).isNotEmpty();
        assertThat(dto.getSwaggerDefinition()).isNotBlank();
    }

    @Test
    public void parseWithMissingRequiredFields(){
        ApiDTO dto = ApiSpecParserService.parse(incompleteSpec);
        assertThat(dto).isNotNull();
        assertThat(dto.getSwaggerDefinition()).isNotNull();
        assertThat(dto.getName()).isNullOrEmpty();
        assertThat(dto.getDescription()).isNullOrEmpty();
        assertThat(dto.getVersion()).isNullOrEmpty();
    }

    @Test
    public void parseInvalidFormat(){
        assertThatExceptionOfType(ApiParsingException.class).isThrownBy(
                () -> ApiSpecParserService.parse(invalidSpec)
        );
    }

    @Test
    public void parseInvalidSwaggerSpec(){
        String invalidSwaggerSpec = getFile("apis/invalid_swagger.yml");
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> ApiSpecParserService.parse(invalidSwaggerSpec)
        );
    }

    private String getFile(String fileName){
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
