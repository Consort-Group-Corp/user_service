package uz.consortgroup.userservice.service.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MentorUserSearchLoggerStrategy implements UserSearchActionLoggerStrategy {
    private final MentorActionLogger mentorLogger;

    @Override
    public void log(UUID actorId, User targetUser) {
        log.info("Mentor (actorId={}) searching user (targetUserId={})", actorId, targetUser.getId());
        mentorLogger.logUserSearch(actorId, targetUser.getId());
    }

    @Override
    public UserRole getSupportedRole() {
        return UserRole.MENTOR;
    }
}
