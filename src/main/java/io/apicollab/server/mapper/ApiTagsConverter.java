package io.apicollab.server.mapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Helper class to convert between Tags:List<String>(ENTITY) to Tags:String (DB)
 */
@Converter
public class ApiTagsConverter implements AttributeConverter<List<String>, String> {
    private static String TAG_DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<String> tags) {
        if(tags == null) return null;
        return String.join(TAG_DELIMITER, tags);
    }

    @Override
    public List<String> convertToEntityAttribute(String s) {
        if(s == null) return null;
        return asList(s.split(TAG_DELIMITER));
    }
}
