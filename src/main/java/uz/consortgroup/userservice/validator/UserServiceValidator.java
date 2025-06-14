package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
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
public class UserServiceValidator {
    private final UserRepository userRepository;

    public void validateUserRegistration(UserRegistrationRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + dto.getEmail() + " already exists");
        }
    }

    public void validateUserId(UUID userId) {
        if (userId == null) {
            throw new UserNotFoundException(String.format("User with id %s not found", userId));
        }
    }


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

    public void validateAllUsersExist(List<UUID> userIds) {
        List<UUID> existingIds = userRepository.findAllById(userIds).stream()
                .map(User::getId)
                .toList();

        List<UUID> notFound = userIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        if (!notFound.isEmpty()) {
            throw new UserNotFoundException("Users not found: " + notFound);
        }
    }

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
