package uz.consortgroup.userservice.service.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.service.event.admin.SuperAdminActionLogger;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SuperAdminUserSearchLoggerStrategyTest {

    private SuperAdminActionLogger superAdminActionLogger;
    private SuperAdminUserSearchLoggerStrategy strategy;

    @BeforeEach
    void setUp() {
        superAdminActionLogger = mock(SuperAdminActionLogger.class);
        strategy = new SuperAdminUserSearchLoggerStrategy(superAdminActionLogger);
    }

    @Test
    void log_shouldCallSuperAdminLogger_whenValidInput() {
        UUID actorId = UUID.randomUUID();
        User targetUser = new User();
        targetUser.setId(UUID.randomUUID());

        strategy.log(actorId, targetUser);

        verify(superAdminActionLogger, times(1)).logUserSearch(targetUser, actorId);
    }

    @Test
    void getSupportedRole_shouldReturnSUPER_ADMIN() {
        UserRole role = strategy.getSupportedRole();
        assert role == UserRole.SUPER_ADMIN;
    }
}
