package uz.consortgroup.userservice.dto.super_admin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.userservice.deserializer.LanguageDeserializer;
import uz.consortgroup.userservice.entity.enumeration.Language;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LoginRequest {
    @JsonDeserialize(using = LanguageDeserializer.class)
    private Language language;
    private String email;
    private String password;
}
