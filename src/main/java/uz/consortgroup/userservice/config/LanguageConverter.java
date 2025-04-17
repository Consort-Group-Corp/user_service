
package uz.consortgroup.userservice.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uz.consortgroup.userservice.entity.enumeration.Language;

@Converter(autoApply = true)
public class LanguageConverter implements AttributeConverter<Language, String> {

    @Override
    public String convertToDatabaseColumn(Language attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public Language convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Language.fromCode(dbData);
    }
}
