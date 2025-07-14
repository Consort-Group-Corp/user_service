package uz.consortgroup.userservice.service.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSearchLoggerStrategyFactoryTest {

    private UserSearchLoggerStrategyFactory factory;
    private UserSearchActionLoggerStrategy hrStrategy;

    @BeforeEach
    void setUp() {
        hrStrategy = mock(UserSearchActionLoggerStrategy.class);
        when(hrStrategy.getSupportedRole()).thenReturn(UserRole.HR);

        UserSearchActionLoggerStrategy mentorStrategy = mock(UserSearchActionLoggerStrategy.class);
        when(mentorStrategy.getSupportedRole()).thenReturn(UserRole.MENTOR);

        factory = new UserSearchLoggerStrategyFactory(List.of(hrStrategy, mentorStrategy));
    }

    @Test
    void get_shouldReturnStrategy_whenRoleExists() {
        Optional<UserSearchActionLoggerStrategy> result = factory.get(UserRole.HR);
        assertTrue(result.isPresent());
        assertEquals(hrStrategy, result.get());
    }

    @Test
    void get_shouldReturnEmpty_whenRoleNotExists() {
        Optional<UserSearchActionLoggerStrategy> result = factory.get(UserRole.STUDENT);
        assertTrue(result.isEmpty());
    }
}
