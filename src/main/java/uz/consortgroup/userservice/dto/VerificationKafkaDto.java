package uz.consortgroup.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.userservice.event.EventType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VerificationKafkaDto {
    private Long messageId;
    private Long userId;
    private String firstName;
    private String middleName;
    private String email;
    private String verificationCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private EventType eventType;
}
