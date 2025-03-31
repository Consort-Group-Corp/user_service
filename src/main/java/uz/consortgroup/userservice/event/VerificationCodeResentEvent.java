package uz.consortgroup.userservice.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCodeResentEvent {
    private Long messageId;
    private Long userId;
    private String email;
    private String newVerificationCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private EventType eventType;
}
