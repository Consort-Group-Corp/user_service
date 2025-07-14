package uz.consortgroup.userservice.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.service.event.hr.HrActionLogger;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HrUserSearchLoggerStrategy implements UserSearchActionLoggerStrategy {
    private final HrActionLogger hrLogger;

    @Override
    public void log(UUID actorId, User targetUser) {
        hrLogger.logUserSearch(actorId, targetUser.getId());
    }

    @Override
    public UserRole getSupportedRole() {
        return UserRole.HR;
    }
}
