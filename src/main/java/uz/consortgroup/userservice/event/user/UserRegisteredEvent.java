package uz.consortgroup.userservice.event.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Language;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private UUID messageId;
    @JsonProperty("userId")
    private UUID userId;
    @JsonProperty("language")
    private Language language;
    private String email;
    private String verificationCode;
    @JsonProperty("eventType")
    private EventType eventType;
}
