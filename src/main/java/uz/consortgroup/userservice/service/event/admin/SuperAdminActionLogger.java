package uz.consortgroup.userservice.service.event.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.event.admin.SuperAdminActionType;
import uz.consortgroup.userservice.event.admin.SuperAdminActionEvent;
import uz.consortgroup.userservice.kafka.SuperAdminActionLogProducer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminActionLogger {
    private final SuperAdminActionLogProducer superAdminActionLogProducer;

    public void userRoleChangedEvent(User user, UUID adminId, SuperAdminActionType superAdminActionType) {
        SuperAdminActionEvent event = SuperAdminActionEvent.builder()
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


    public void logUserSearch(User targetUser, UUID adminId) {
        SuperAdminActionEvent event = SuperAdminActionEvent.builder()
                .messageId(UUID.randomUUID())
                .userId(targetUser.getId())
                .adminId(adminId)
                .email(targetUser.getEmail())
                .role(targetUser.getRole())
                .superAdminActionType(SuperAdminActionType.SEARCH_USER)
                .createdAt(LocalDateTime.now())
                .build();

        superAdminActionLogProducer.sendSuperAdminActionEvents(List.of(event));
    }
}
