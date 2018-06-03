package io.apicollab.server.mapper;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

public class ApiTagsConverterTest {

    private ApiTagsConverter apiTagsConverter = new ApiTagsConverter();

    @Test
    public void convertToDatabaseColumnWithNullInput() {
        assertThat(apiTagsConverter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    public void convertToDatabaseColumnWithBlankInput() {
        assertThat(apiTagsConverter.convertToDatabaseColumn(Arrays.asList(" ", null))).isEqualTo(" ,null");
    }

    @Test
    public void convertToDatabaseColumn() {
        assertThat(apiTagsConverter.convertToDatabaseColumn(Arrays.asList("tag1", "tag3", "tag2"))).isEqualTo("tag1,tag3,tag2");
    }

    @Test
    public void convertToEntityAttributeWithNullInput() {
        assertThat(apiTagsConverter.convertToEntityAttribute(null)).isEmpty();
    }

    @Test
    public void convertToEntityAttributeWithBlankInput() {
        assertThat(apiTagsConverter.convertToEntityAttribute(" ")).containsExactlyElementsOf(newArrayList(" "));
    }

    @Test
    public void convertToEntityAttribute() {
        assertThat(apiTagsConverter.convertToEntityAttribute("tag1,tag3,tag2")).containsExactlyElementsOf(newArrayList("tag1", "tag3", "tag2"));
    }
}
