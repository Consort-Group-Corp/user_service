package uz.consortgroup.userservice.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.dto.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.InvalidTokenException;
import uz.consortgroup.userservice.service.operation.PasswordOperationsService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {

    @Mock
    private PasswordOperationsService passwordOperationsService;

    @InjectMocks
    private PasswordValidator passwordValidator;

    @Test
    void validateTokenUserMatch_ShouldAcceptMatchingUserId() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        assertDoesNotThrow(() -> 
            passwordValidator.validateTokenUserMatch(userId, userId.toString(), user));
    }

    @Test
    void validateTokenUserMatch_ShouldThrowWhenUserIdMismatch() {
        UUID userId = UUID.randomUUID();
        UUID tokenUserId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        assertThrows(RuntimeException.class, () -> 
            passwordValidator.validateTokenUserMatch(userId, tokenUserId.toString(), user));
    }

    @Test
    void validateTokenUserMatch_ShouldAcceptMatchingEmail() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setEmail("test@example.com");

        assertDoesNotThrow(() -> 
            passwordValidator.validateTokenUserMatch(userId, "test@example.com", user));
    }

    @Test
    void validateTokenUserMatch_ShouldThrowWhenEmailMismatch() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setEmail("test@example.com");

        assertThrows(RuntimeException.class, () -> 
            passwordValidator.validateTokenUserMatch(userId, "wrong@example.com", user));
    }

    @Test
    void validatePasswordAndToken_ShouldAcceptValidInput() {
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setNewPassword("password123");
        request.setConfirmPassword("password123");
        String token = "valid-token";

        when(passwordOperationsService.validatePasswordResetToken(token)).thenReturn(true);

        assertDoesNotThrow(() -> 
            passwordValidator.validatePasswordAndToken(request, token));
    }

    @Test
    void validatePasswordAndToken_ShouldThrowWhenPasswordsMismatch() {
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setNewPassword("password123");
        request.setConfirmPassword("different");
        String token = "valid-token";

        assertThrows(RuntimeException.class, () -> 
            passwordValidator.validatePasswordAndToken(request, token));
    }

    @Test
    void validatePasswordAndToken_ShouldThrowWhenTokenInvalid() {
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setNewPassword("password123");
        request.setConfirmPassword("password123");
        String token = "invalid-token";

        when(passwordOperationsService.validatePasswordResetToken(token)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> 
            passwordValidator.validatePasswordAndToken(request, token));
    }

    @Test
    void validatePasswordAndToken_ShouldThrowWhenTokenNull() {
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setNewPassword("password123");
        request.setConfirmPassword("password123");

        assertThrows(InvalidTokenException.class, () -> 
            passwordValidator.validatePasswordAndToken(request, null));
    }
}