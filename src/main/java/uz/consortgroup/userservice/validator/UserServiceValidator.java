package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.request.UserRegistrationRequestDto;
import uz.consortgroup.userservice.exception.UserAlreadyExistsException;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.exception.UserRoleNotFoundException;
import uz.consortgroup.userservice.repository.UserRepository;

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
}
