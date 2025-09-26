package uz.consortgroup.userservice.service.password;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Language;
import uz.consortgroup.core.api.v1.dto.user.request.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.entity.Password;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.PasswordMismatchException;
import uz.consortgroup.userservice.repository.PasswordRepository;
import uz.consortgroup.userservice.security.AuthContext;
import uz.consortgroup.userservice.service.event.user.PasswordEventService;
import uz.consortgroup.userservice.service.operation.PasswordOperationsService;
import uz.consortgroup.userservice.service.operation.UserOperationsService;
import uz.consortgroup.userservice.validator.PasswordValidator;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordServiceImplTest {

    @Mock
    private PasswordRepository passwordRepository;

    @Mock
    private PasswordEventService passwordEventService;

    @Mock
    private PasswordOperationsService passwordOperationsService;

    @Mock
    private UserOperationsService userOperationsService;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private PasswordServiceImpl passwordService;

    @Test
    void savePassword_Success() {
        User user = new User();
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";
        
        when(passwordOperationsService.encodePassword(rawPassword)).thenReturn(encodedPassword);
        when(passwordOperationsService.createPassword(user, encodedPassword)).thenReturn(new Password());
        
        passwordService.savePassword(user, rawPassword);
        
        verify(passwordRepository).save(any(Password.class));
    }

    @Test
    void requestPasswordReset_Success() {
        UUID userId = authContext.getCurrentUserId();
        User user = new User();
        user.setEmail("test@example.com");
        user.setLanguage(Language.ENGLISH);
        String token = "resetToken";
        
        when(userOperationsService.findUserById(userId)).thenReturn(user);
        when(passwordOperationsService.generatePasswordResetToken(user.getEmail())).thenReturn(token);
        
        passwordService.requestPasswordReset();
        
        verify(passwordEventService).sendPasswordEvent(user.getEmail(), userId, token, user.getLanguage());
    }

    @Test
    void updatePassword_Success() {
        UUID userId = UUID.randomUUID();
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setNewPassword("newPassword123");
        String token = "validToken";
        String tokenSubject = userId.toString();
        User user = new User();
        Password password = new Password();

        when(authContext.getCurrentUserId()).thenReturn(userId);
        when(passwordOperationsService.extractUserIdFromToken(token)).thenReturn(tokenSubject);
        when(userOperationsService.findUserById(userId)).thenReturn(user);
        when(passwordRepository.findByUser(user)).thenReturn(Optional.of(password));
        when(passwordOperationsService.encodePassword(request.getNewPassword())).thenReturn("encodedNewPassword");
        
        passwordService.updatePassword(request, token);
        
        verify(passwordValidator).validatePasswordAndToken(request, token);
        verify(passwordValidator).validateTokenUserMatch(userId, tokenSubject, user);
        assertEquals("encodedNewPassword", password.getPasswordHash());
        assertNotNull(password.getUpdatedAt());
    }

    @Test
    void updatePassword_PasswordNotFound() {
        UUID userId = UUID.randomUUID();
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        String token = "validToken";
        User user = new User();

        when(authContext.getCurrentUserId()).thenReturn(userId);
        when(passwordOperationsService.extractUserIdFromToken(token)).thenReturn(userId.toString());
        when(userOperationsService.findUserById(userId)).thenReturn(user);
        when(passwordRepository.findByUser(user)).thenReturn(Optional.empty());
        
        assertThrows(PasswordMismatchException.class, () -> 
            passwordService.updatePassword(request, token));
    }
}