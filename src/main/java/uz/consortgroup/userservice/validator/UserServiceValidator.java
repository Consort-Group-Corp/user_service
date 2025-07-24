package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.request.UserRegistrationRequestDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.UserAlreadyExistsException;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.exception.UserRoleNotFoundException;
import uz.consortgroup.userservice.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceValidator {
    private final UserRepository userRepository;

    public void validateUserRegistration(UserRegistrationRequestDto dto) {
        log.info("Validating registration for email: {}", dto.getEmail());
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("User with email {} already exists", dto.getEmail());
            throw new UserAlreadyExistsException("User with email " + dto.getEmail() + " already exists");
        }
        log.info("Registration validation passed for email: {}", dto.getEmail());
    }

    public void validateUserId(UUID userId) {
        log.info("Validating user ID: {}", userId);
        if (userId == null) {
            log.error("User ID is null");
            throw new UserNotFoundException(String.format("User with id %s not found", userId));
        }
    }

    public void checkUserRole(String role) {
        log.info("Validating user role: {}", role);
        if (role == null) {
            log.error("User role is null");
            throw new UserRoleNotFoundException("Role cannot be null");
        }
        try {
            UserRole.valueOf(role);
            log.info("Role {} is valid", role);
        } catch (IllegalArgumentException e) {
            log.error("Role {} not found in UserRole enum", role);
            throw new UserRoleNotFoundException("Role " + role + " not found in UserRole enum");
        }
    }

    public void validateAllUsersExist(List<UUID> userIds) {
        log.info("Validating existence of users: {}", userIds);
        List<UUID> existingIds = userRepository.findAllById(userIds).stream()
                .map(User::getId)
                .toList();

        List<UUID> notFound = userIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        if (!notFound.isEmpty()) {
            log.warn("Following users not found: {}", notFound);
            throw new UserNotFoundException("Users not found: " + notFound);
        }
        log.info("All users exist");
    }

    public void validatePasswordChangeRequest(Long userId, String oldPassword, String newPassword) {
        log.info("Validating password change for userId: {}", userId);

        if (userId == null) {
            log.error("User ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (oldPassword == null || oldPassword.isBlank()) {
            log.error("Old password is empty");
            throw new IllegalArgumentException("Old password cannot be empty");
        }

        if (newPassword == null || newPassword.isBlank()) {
            log.error("New password is empty");
            throw new IllegalArgumentException("New password cannot be empty");
        }

        if (oldPassword.equals(newPassword)) {
            log.error("New password must be different from old password");
            throw new IllegalArgumentException("New password must be different from old password");
        }

        if (!isPasswordValid(newPassword)) {
            log.error("New password does not meet complexity requirements");
            throw new IllegalArgumentException("Password does not meet complexity requirements");
        }

        log.info("Password change request is valid");
    }

    private boolean isPasswordValid(String password) {
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        return password.matches(pattern);
    }
}
