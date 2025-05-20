package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.request.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.InvalidTokenException;
import uz.consortgroup.userservice.service.operation.PasswordOperationsService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasswordValidator {
    private final PasswordOperationsService passwordOperationsService;

    public void validateTokenUserMatch(UUID userId, String tokenSubject, User user) {
        try {
            UUID tokenUserId = UUID.fromString(tokenSubject);
            if (!userId.equals(tokenUserId)) {
                throw new RuntimeException("Token does not match user ID");
            }
        } catch (IllegalArgumentException e) {
            if (!user.getEmail().equals(tokenSubject)) {
                throw new RuntimeException("Token does not match user email");
            }
        }
    }

    public void validatePasswordAndToken(UpdatePasswordRequestDto request, String token) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (!passwordOperationsService.validatePasswordResetToken(token)) {
            throw new InvalidTokenException("Invalid or expired token");
        }
    }
}
