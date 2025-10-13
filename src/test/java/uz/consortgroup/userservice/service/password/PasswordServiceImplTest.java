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
}