package uz.consortgroup.userservice.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PasswordResetRequestedEvent {
    private Long messageId;
    private String email;
    private String token;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private EventType eventType;
    private String resetLink;
}
