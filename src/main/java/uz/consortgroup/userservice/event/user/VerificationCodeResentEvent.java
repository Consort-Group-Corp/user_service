package uz.consortgroup.userservice.event.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Language;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCodeResentEvent {
    @JsonProperty("messageId")
    private UUID messageId;
    private UUID userId;
    private String email;
    private String newVerificationCode;
    @JsonProperty("language")
    private Language language;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private EventType eventType;
}
