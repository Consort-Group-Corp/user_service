package uz.consortgroup.userservice.service.event.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.event.admin.SuperAdminActionType;
import uz.consortgroup.userservice.event.admin.SuperAdminUserActionEvent;
import uz.consortgroup.userservice.kafka.SuperAdminActionLogProducer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminActionLogger {
    private final SuperAdminActionLogProducer superAdminActionLogProducer;

    public void userRoleChangedEvent(User user, UUID adminId, SuperAdminActionType superAdminActionType) {
        SuperAdminUserActionEvent event = SuperAdminUserActionEvent.builder()
                .messageId(UUID.randomUUID())
                .userId(user.getId())
                .adminId(adminId)
                .email(user.getEmail())
                .role(user.getRole())
                .superAdminActionType(superAdminActionType)
                .createdAt(LocalDateTime.now())
                .build();

        superAdminActionLogProducer.sendSuperAdminActionEvents(List.of(event));
    }
}
