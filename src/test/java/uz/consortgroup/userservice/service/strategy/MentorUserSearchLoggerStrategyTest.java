package uz.consortgroup.userservice.service.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MentorUserSearchLoggerStrategyTest {

    private MentorActionLogger mentorActionLogger;
    private MentorUserSearchLoggerStrategy strategy;

    @BeforeEach
    void setUp() {
        mentorActionLogger = mock(MentorActionLogger.class);
        strategy = new MentorUserSearchLoggerStrategy(mentorActionLogger);
    }

    @Test
    void log_shouldCallMentorLogger_whenValidInput() {
        UUID actorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User targetUser = new User();
        targetUser.setId(userId);

        strategy.log(actorId, targetUser);

        verify(mentorActionLogger, times(1)).logUserSearch(actorId, userId);
    }

    @Test
    void getSupportedRole_shouldReturnMENTOR() {
        UserRole role = strategy.getSupportedRole();
        assert role == UserRole.MENTOR;
    }
}
