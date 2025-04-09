package uz.consortgroup.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.consortgroup.userservice.entity.Password;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.repository.PasswordRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class PasswordServiceTest {
    @Mock
    private PasswordRepository passwordRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordService passwordService;


    @Test
    void testSavePassword_Success() {
        User user = new User();
        String rawPassword = "mySecretPassword";
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);


        passwordService.savePassword(user, rawPassword);

        ArgumentCaptor<Password> captor = ArgumentCaptor.forClass(Password.class);
        verify(passwordRepository, times(1)).save(captor.capture());

        Password savedPassword = captor.getValue();

        assertEquals(encodedPassword, savedPassword.getPasswordHash());
        assertEquals(user, savedPassword.getUser());
        assertTrue(savedPassword.getIsActive());
        assertNotNull(savedPassword.getCreatedAt());
    }

    @Test
    void testSavePassword_WhenEncoderFails_ShouldThrowException() {
        User user = new User();
        String rawPassword = "failPassword";

        when(passwordEncoder.encode(rawPassword)).thenThrow(new RuntimeException("Encoding failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            passwordService.savePassword(user, rawPassword);
        });

        assertEquals("Encoding failed", ex.getMessage());
        verify(passwordRepository, never()).save(any());
    }
}
