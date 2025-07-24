package uz.consortgroup.userservice.service.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.service.event.hr.HrActionLogger;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class HrUserSearchLoggerStrategy implements UserSearchActionLoggerStrategy {
    private final HrActionLogger hrLogger;

    @Override
    public void log(UUID actorId, User targetUser) {
        log.info("HR (actorId={}) searching for user (targetUserId={})", actorId, targetUser.getId());
        hrLogger.logUserSearch(actorId, targetUser.getId());
    }

    @Override
    public UserRole getSupportedRole() {
        return UserRole.HR;
    }
}
