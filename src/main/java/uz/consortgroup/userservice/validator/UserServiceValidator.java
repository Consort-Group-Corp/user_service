package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.dto.UserRegistrationDto;
import uz.consortgroup.userservice.entity.enumeration.UserRole;
import uz.consortgroup.userservice.exception.UserAlreadyExistsException;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.exception.UserRoleNotFoundException;
import uz.consortgroup.userservice.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserServiceValidator {
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void validateUserRegistration(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + dto.getEmail() + " already exists");
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void validateUserId(Long userId) {
        if (userId == null) {
            throw new UserNotFoundException(String.format("User with id %s not found", userId));
        }
    }


    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void checkUserRole(String role) {
        if (role == null) {
            throw new UserRoleNotFoundException("Role cannot be null");
        }
        try {
            UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new UserRoleNotFoundException("Role " + role + " not found in UserRole enum");
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void validatePasswordChangeRequest(Long userId, String oldPassword, String newPassword) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("Old password cannot be empty");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from old password");
        }

        if (!isPasswordValid(newPassword)) {
            throw new IllegalArgumentException("Password does not meet complexity requirements");
        }
    }

    private boolean isPasswordValid(String password) {
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        return password.matches(pattern);
    }
}
