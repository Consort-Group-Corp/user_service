package uz.consortgroup.userservice.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRegistrationEvent {
    @JsonProperty("messageId")
    private Long messageId;
    private Long userId;
    private String firstName;
    private String middleName;
    private String email;
    private String verificationCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private EventType eventType;
}
