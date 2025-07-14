package uz.consortgroup.userservice.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MentorUserSearchLoggerStrategy implements UserSearchActionLoggerStrategy {
    private final MentorActionLogger mentorLogger;

    @Override
    public void log(UUID actorId, User targetUser) {
        mentorLogger.logUserSearch(actorId, targetUser.getId());
    }

    @Override
    public UserRole getSupportedRole() {
        return UserRole.MENTOR;
    }
}
