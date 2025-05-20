package uz.consortgroup.userservice.event.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Language;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PasswordResetRequestedEvent {
    private UUID messageId;
    private UUID userId;
    private String email;
    private String token;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private EventType eventType;
    private String resetLink;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Language language;
}
