package uz.consortgroup.userservice.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.request.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.InvalidOrExpiredResetTokenException;
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

        assertThrows(InvalidOrExpiredResetTokenException.class, () ->
            passwordValidator.validatePasswordAndToken(request, token));
    }

    @Test
    void validatePasswordAndToken_ShouldThrowWhenTokenNull() {
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setNewPassword("password123");
        request.setConfirmPassword("password123");

        assertThrows(InvalidOrExpiredResetTokenException.class, () ->
            passwordValidator.validatePasswordAndToken(request, null));
    }
}