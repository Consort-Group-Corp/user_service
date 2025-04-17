package uz.consortgroup.userservice.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import uz.consortgroup.userservice.entity.enumeration.Language;

import java.io.IOException;

public class LanguageDeserializer extends JsonDeserializer<Language> {

    @Override
    public Language deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String code = p.getText();
        return Language.fromCode(code); // Использует твой метод
    }
}
