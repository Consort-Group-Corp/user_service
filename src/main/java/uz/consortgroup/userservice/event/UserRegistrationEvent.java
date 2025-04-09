package uz.consortgroup.userservice.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.userservice.entity.enumeration.Language;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRegistrationEvent {
    @JsonProperty("language")
    private Language language;
    @JsonProperty("messageId")
    private Long messageId;
    private Long userId;
    private String lastName;
    private String firstName;
    private String middleName;
    private String email;
    private String phoneNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate bornDate;
    private String verificationCode;
    @JsonProperty("communication")
    private Communication communication;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private EventType eventType;
}
