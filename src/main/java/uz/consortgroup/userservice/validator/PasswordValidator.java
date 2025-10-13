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

    public void validatePasswordAndToken(UpdatePasswordRequestDto request, String token) {
        if (request == null || token == null || token.isBlank()) {
            throw new InvalidOrExpiredResetTokenException("Invalid or expired token");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.debug("Password confirmation mismatch");
            throw new PasswordsDoNotMatchException("Passwords do not match");
        }

        boolean ok = passwordOperationsService.validatePasswordResetToken(token);
        if (!ok) {
            log.debug("Reset token validation failed");
            throw new InvalidOrExpiredResetTokenException("Invalid or expired token");
        }

        log.debug("Reset token and password payload validated");
    }
}
