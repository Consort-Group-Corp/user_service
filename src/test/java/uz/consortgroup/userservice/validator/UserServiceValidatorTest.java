package uz.consortgroup.userservice.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.dto.UserRegistrationDto;
import uz.consortgroup.userservice.entity.enumeration.UserRole;
import uz.consortgroup.userservice.exception.UserAlreadyExistsException;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.exception.UserRoleNotFoundException;
import uz.consortgroup.userservice.repository.UserRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceValidatorTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceValidator userServiceValidator;

    @Test
    void validateUserRegistration_WhenEmailNotExists_ShouldNotThrowException() {
        UserRegistrationDto dto = UserRegistrationDto.builder()
                .email("new@example.com")
                .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);

        assertThatNoException()
                .isThrownBy(() -> userServiceValidator.validateUserRegistration(dto));

        verify(userRepository).existsByEmail(dto.getEmail());
    }

    @Test
    void validateUserRegistration_WhenEmailExists_ShouldThrowUserAlreadyExistsException() {
        String email = "existing@example.com";
        UserRegistrationDto dto = UserRegistrationDto.builder()
                .email(email)
                .build();

        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> userServiceValidator.validateUserRegistration(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository).existsByEmail(email);
    }

    @Test
    void validateUserId_WhenUserIdIsValid_ShouldNotThrowException() {
        Long userId = 1L;

        assertThatNoException()
                .isThrownBy(() -> userServiceValidator.validateUserId(userId));
    }

    @Test
    void validateUserId_WhenUserIdIsNull_ShouldThrowUserNotFoundException() {
        Long userId = null;

        assertThatThrownBy(() -> userServiceValidator.validateUserId(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void checkUserRole_WhenRoleIsValid_ShouldNotThrowException() {
        String validRole = UserRole.STUDENT.name();

        assertThatNoException()
                .isThrownBy(() -> userServiceValidator.checkUserRole(validRole));
    }

    @Test
    void checkUserRole_WhenRoleIsInvalid_ShouldThrowUserRoleNotFoundException() {
        String invalidRole = "INVALID_ROLE";

        assertThatThrownBy(() -> userServiceValidator.checkUserRole(invalidRole))
                .isInstanceOf(UserRoleNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void checkUserRole_WhenRoleIsNull_ShouldThrowUserRoleNotFoundException() {
        assertThatThrownBy(() -> userServiceValidator.checkUserRole(null))
                .isInstanceOf(UserRoleNotFoundException.class)
                .hasMessageContaining("Role cannot be null")
                .isNotInstanceOf(NullPointerException.class);
    }
}
