package uz.consortgroup.userservice.event.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.userservice.entity.enumeration.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserCreatedEvent {
    private UUID messageId;
    private UUID adminId;
    private UUID userId;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private UserRole role;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ActionType actionType;
    private LocalDateTime createdAt;
}
