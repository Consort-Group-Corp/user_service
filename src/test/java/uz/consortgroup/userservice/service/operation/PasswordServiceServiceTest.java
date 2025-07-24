package uz.consortgroup.userservice.service.operation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.consortgroup.userservice.entity.Password;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.service.operation.PasswordOperationsService;
import uz.consortgroup.userservice.util.JwtUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private PasswordOperationsService passwordOperationsService;

    @Test
    void encodePassword_ShouldReturnEncodedPassword() {
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        String result = passwordOperationsService.encodePassword(rawPassword);

        assertEquals(encodedPassword, result);
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void createPassword_ShouldCreatePasswordWithCorrectFields() {
        User user = new User();
        user.setId(UUID.randomUUID());
        String encodedPassword = "encodedPassword123";

        Password result = passwordOperationsService.createPassword(user, encodedPassword);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(encodedPassword, result.getPasswordHash());
        assertTrue(result.getIsActive());
        assertNotNull(result.getCreatedAt());
        assertTrue(result.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)) || 
                  result.getCreatedAt().isEqual(LocalDateTime.now()));
    }

    @Test
    void generatePasswordResetToken_ShouldCallJwtUtils() {
        String email = "test@example.com";
        String expectedToken = "generatedToken";
        when(jwtUtils.generatePasswordResetToken(email)).thenReturn(expectedToken);

        String result = passwordOperationsService.generatePasswordResetToken(email);

        assertEquals(expectedToken, result);
        verify(jwtUtils).generatePasswordResetToken(email);
    }

    @Test
    void validatePasswordResetToken_ShouldReturnTrueForValidToken() {
        String token = "validToken";
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);

        boolean result = passwordOperationsService.validatePasswordResetToken(token);

        assertTrue(result);
        verify(jwtUtils).validateJwtToken(token);
    }

    @Test
    void validatePasswordResetToken_ShouldReturnFalseForInvalidToken() {
        String token = "invalidToken";
        when(jwtUtils.validateJwtToken(token)).thenReturn(false);

        boolean result = passwordOperationsService.validatePasswordResetToken(token);

        assertFalse(result);
        verify(jwtUtils).validateJwtToken(token);
    }

    @Test
    void extractUserIdFromToken_ShouldReturnEmail() {
        String token = "testToken";
        String expectedEmail = "test@example.com";
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn(expectedEmail);

        String result = passwordOperationsService.extractUserIdFromToken(token);

        assertEquals(expectedEmail, result);
        verify(jwtUtils).getUserNameFromJwtToken(token);
    }

    @Test
    void encodePassword_ShouldHandleNullPassword() {
        when(passwordEncoder.encode(null)).thenThrow(new IllegalArgumentException());

        assertThrows(IllegalArgumentException.class, () -> {
            passwordOperationsService.encodePassword(null);
        });

        verify(passwordEncoder).encode(null);
    }

    @Test
    void createPassword_ShouldHandleNullEncodedPassword() {
        User user = new User();
        Password result = passwordOperationsService.createPassword(user, null);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertNull(result.getPasswordHash());
        assertTrue(result.getIsActive());
        assertNotNull(result.getCreatedAt());
    }
}