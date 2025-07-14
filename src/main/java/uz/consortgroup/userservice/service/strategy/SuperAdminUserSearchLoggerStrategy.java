package uz.consortgroup.userservice.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.service.event.admin.SuperAdminActionLogger;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SuperAdminUserSearchLoggerStrategy implements UserSearchActionLoggerStrategy {
    private final SuperAdminActionLogger superAdminLogger;

    @Override
    public void log(UUID actorId, User targetUser) {
        superAdminLogger.logUserSearch(targetUser, actorId);
    }

    @Override
    public UserRole getSupportedRole() {
        return UserRole.SUPER_ADMIN;
    }
}
