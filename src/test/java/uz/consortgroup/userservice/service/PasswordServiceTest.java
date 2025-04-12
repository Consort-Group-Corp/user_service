package uz.consortgroup.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.dto.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.entity.Password;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.InvalidTokenException;
import uz.consortgroup.userservice.exception.PasswordMismatchException;
import uz.consortgroup.userservice.repository.PasswordRepository;
import uz.consortgroup.userservice.service.event.PasswordEventService;
import uz.consortgroup.userservice.service.operation.PasswordOperationsService;
import uz.consortgroup.userservice.service.operation.UserOperations;
import uz.consortgroup.userservice.validator.PasswordValidator;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordRepository passwordRepository;

    @Mock
    private PasswordEventService passwordEventService;

    @Mock
    private PasswordOperationsService passwordOperationsService;

    @Mock
    private UserOperations userOperations;

    @Mock
    private PasswordValidator passwordValidator;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    void savePassword_ShouldSaveEncodedPassword() {
        User user = new User();
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";
        Password password = new Password();

        when(passwordOperationsService.encodePassword(rawPassword)).thenReturn(encodedPassword);
        when(passwordOperationsService.createPassword(user, encodedPassword)).thenReturn(password);

        passwordService.savePassword(user, rawPassword);

        verify(passwordOperationsService).encodePassword(rawPassword);
        verify(passwordOperationsService).createPassword(user, encodedPassword);
        verify(passwordRepository).save(password);
    }

    @Test
    void requestPasswordReset_ShouldGenerateTokenAndSendEvent() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setEmail("test@example.com");
        String token = "generatedToken";
        
        when(userOperations.findUserById(userId)).thenReturn(user);
        when(passwordOperationsService.generatePasswordResetToken(user.getEmail())).thenReturn(token);
        
        passwordService.requestPasswordReset(userId);
        
        verify(userOperations).findUserById(userId);
        verify(passwordOperationsService).generatePasswordResetToken(user.getEmail());
        verify(passwordEventService).sendPasswordEvent(user.getEmail(), token);
    }

    @Test
    void updatePassword_ShouldUpdatePasswordWhenValid() {
        UUID userId = UUID.randomUUID();
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setNewPassword("newPassword123");
        String token = "validToken";
        User user = new User();
        user.setId(userId);
        Password password = new Password();
        String encodedPassword = "encodedNewPassword123";
        String tokenSubject = user.getEmail();
        
        when(userOperations.findUserById(userId)).thenReturn(user);
        when(passwordOperationsService.extractUserIdFromToken(token)).thenReturn(tokenSubject);
        when(passwordRepository.findByUser(user)).thenReturn(Optional.of(password));
        when(passwordOperationsService.encodePassword(request.getNewPassword())).thenReturn(encodedPassword);
        
        passwordService.updatePassword(userId, request, token);
        
        verify(passwordValidator).validatePasswordAndToken(request, token);
        verify(passwordValidator).validateTokenUserMatch(userId, tokenSubject, user);
        assertEquals(encodedPassword, password.getPasswordHash());
        assertNotNull(password.getUpdatedAt());
        verify(passwordRepository).save(password);
    }

    @Test
    void updatePassword_ShouldThrowWhenPasswordNotFound() {
        UUID userId = UUID.randomUUID();
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setNewPassword("newPassword123");
        String token = "validToken";
        User user = new User();
        user.setId(userId);
        String tokenSubject = user.getEmail();
        
        when(userOperations.findUserById(userId)).thenReturn(user);
        when(passwordOperationsService.extractUserIdFromToken(token)).thenReturn(tokenSubject);
        when(passwordRepository.findByUser(user)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            passwordService.updatePassword(userId, request, token);
        });
        
        verify(passwordRepository).findByUser(user);
    }

    @Test
    void updatePassword_ShouldThrowWhenTokenInvalid() {
        UUID userId = UUID.randomUUID();
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        String token = "invalidToken";
        
        doThrow(new InvalidTokenException("Invalid token"))
            .when(passwordValidator).validatePasswordAndToken(request, token);
        
        assertThrows(InvalidTokenException.class, () -> {
            passwordService.updatePassword(userId, request, token);
        });
    }

    @Test
    void updatePassword_ShouldThrowWhenTokenUserMismatch() {
        UUID userId = UUID.randomUUID();
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        String token = "validToken";
        User user = new User();
        user.setId(userId);
        String tokenSubject = "different@email.com";
        
        when(userOperations.findUserById(userId)).thenReturn(user);
        when(passwordOperationsService.extractUserIdFromToken(token)).thenReturn(tokenSubject);
        
        doThrow(new PasswordMismatchException("Token user mismatch"))
            .when(passwordValidator).validateTokenUserMatch(userId, tokenSubject, user);
        
        assertThrows(PasswordMismatchException.class, () -> {
            passwordService.updatePassword(userId, request, token);
        });
    }
}