package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.request.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.InvalidOrExpiredResetTokenException;
import uz.consortgroup.userservice.exception.InvalidTokenException;
import uz.consortgroup.userservice.exception.PasswordsDoNotMatchException;
import uz.consortgroup.userservice.exception.ResetTokenUserMismatchException;
import uz.consortgroup.userservice.service.operation.PasswordOperationsService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordValidator {
    private final PasswordOperationsService passwordOperationsService;

    public void validateTokenUserMatch(UUID userId, String tokenSubject, User user) {
        try {
            UUID tokenUserId = UUID.fromString(tokenSubject);
            if (!userId.equals(tokenUserId)) {
                log.error("Token user ID {} does not match actual user ID {}", tokenUserId, userId);
                throw new ResetTokenUserMismatchException("Token does not match user ID");
            }
            log.info("Token matches user ID {}", userId);
        } catch (IllegalArgumentException e) {
            if (!user.getEmail().equals(tokenSubject)) {
                log.error("Token subject {} does not match user email {}", tokenSubject, user.getEmail());
                throw new ResetTokenUserMismatchException("Token does not match user email");
            }
            log.info("Token matches user email {}", user.getEmail());
        }
    }

    public void validatePasswordAndToken(UpdatePasswordRequestDto request, String token) {
        log.info("Validating password update request for token: {}", token);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.error("New password and confirmation password do not match");
            throw new PasswordsDoNotMatchException("Passwords do not match");
        }

        if (!passwordOperationsService.validatePasswordResetToken(token)) {
            log.error("Invalid or expired token: {}", token);
            throw new InvalidOrExpiredResetTokenException("Invalid or expired token");
        }

        log.info("Password and token are valid");
    }
}
