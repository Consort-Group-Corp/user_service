package uz.consortgroup.userservice.service.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.service.event.hr.HrActionLogger;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HrUserSearchLoggerStrategyTest {

    private HrActionLogger hrActionLogger;
    private HrUserSearchLoggerStrategy strategy;

    @BeforeEach
    void setUp() {
        hrActionLogger = mock(HrActionLogger.class);
        strategy = new HrUserSearchLoggerStrategy(hrActionLogger);
    }

    @Test
    void log_shouldCallHrLogger_whenValidInput() {
        UUID actorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User targetUser = new User();
        targetUser.setId(userId);

        strategy.log(actorId, targetUser);

        verify(hrActionLogger, times(1)).logUserSearch(actorId, userId);
    }

    @Test
    void getSupportedRole_shouldReturnHR() {
        UserRole role = strategy.getSupportedRole();
        assert role == UserRole.HR;
    }
}
