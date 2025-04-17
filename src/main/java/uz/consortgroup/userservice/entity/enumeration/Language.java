package uz.consortgroup.userservice.entity.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum Language {
    ENGLISH("en"),
    RUSSIAN("ru"),
    UZBEK("uz");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    @JsonValue
    public String toCode() {
        return code;
    }

    @JsonCreator
    public static Language fromCode(String code) {
        for (Language language : values()) {
            if (language.getCode().equalsIgnoreCase(code)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Unknown language code: " + code);
    }
}
