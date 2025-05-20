package uz.consortgroup.userservice.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Language;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.request.UserProfileRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserRegistrationRequestDto;
import uz.consortgroup.userservice.exception.UserAlreadyExistsException;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.exception.UserRoleNotFoundException;
import uz.consortgroup.userservice.repository.UserRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplValidatorTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceValidator userServiceValidator;

    private UserProfileRequestDto createValidUserProfileDto() {
        return UserProfileRequestDto.builder()
                .lastName("Doe")
                .firstName("John")
                .middleName("Middle")
                .bornDate(LocalDate.of(1990, 1, 1))
                .phoneNumber("+998901234567")
                .workPlace("Company")
                .position("Developer")
                .pinfl("12345678901234")
                .build();
    }

    private UserRegistrationRequestDto createValidUserRegistrationDto() {
        return UserRegistrationRequestDto.builder()
                .email("test@example.com")
                .password("Password123!")
                .language(Language.ENGLISH)
                .build();
    }

    @Test
    void validateUserRegistration_WhenEmailNotExists_ShouldNotThrowException() {
        UserRegistrationRequestDto dto = createValidUserRegistrationDto();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);

        assertThatNoException()
                .isThrownBy(() -> userServiceValidator.validateUserRegistration(dto));

        verify(userRepository).existsByEmail(dto.getEmail());
    }

    @Test
    void validateUserRegistration_WhenEmailExists_ShouldThrowUserAlreadyExistsException() {
        UserRegistrationRequestDto dto = createValidUserRegistrationDto();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userServiceValidator.validateUserRegistration(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository).existsByEmail(dto.getEmail());
    }

    @Test
    void validateUserId_WhenUserIdIsValid_ShouldNotThrowException() {
        UUID userId = UUID.randomUUID();

        assertThatNoException()
                .isThrownBy(() -> userServiceValidator.validateUserId(userId));
    }

    @Test
    void validateUserId_WhenUserIdIsNull_ShouldThrowUserNotFoundException() {
        UUID userId = null;

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