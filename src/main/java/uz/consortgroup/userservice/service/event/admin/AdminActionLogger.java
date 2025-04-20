package uz.consortgroup.userservice.service.event.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.event.admin.ActionType;
import uz.consortgroup.userservice.event.admin.UserCreatedEvent;
import uz.consortgroup.userservice.kafka.AdminActionLogProducer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminActionLogger {
    private final AdminActionLogProducer adminActionLogProducer;

    public void logUserCreationByAdmin(User user, UUID adminId, ActionType actionType) {
        UserCreatedEvent event = UserCreatedEvent.builder()
                .messageId(UUID.randomUUID())
                .userId(user.getId())
                .adminId(adminId)
                .email(user.getEmail())
                .role(user.getRole())
                .actionType(actionType)
                .createdAt(LocalDateTime.now())
                .build();

        adminActionLogProducer.sendUserCreatedEvents(List.of(event));
    }
}
