package uz.consortgroup.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.VerificationCode;
import uz.consortgroup.userservice.entity.cacheEntity.VerificationCodeCacheEntity;
import uz.consortgroup.userservice.entity.enumeration.VerificationCodeStatus;
import uz.consortgroup.userservice.exception.InvalidVerificationCodeException;
import uz.consortgroup.userservice.exception.VerificationCodeExpiredException;
import uz.consortgroup.userservice.mapper.VerificationCodeCacheMapper;
import uz.consortgroup.userservice.repository.VerificationCodeRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private VerificationCodeCacheService verificationCodeCacheService;

    @Mock
    private VerificationCodeCacheMapper verificationCodeCacheMapper;

    @InjectMocks
    private VerificationService verificationService;

    private User createTestUser(Long id) {
        return User.builder()
                .id(id)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .build();
    }

    private VerificationCode createTestCode(Long id, User user, String codeValue) {
        LocalDateTime now = LocalDateTime.now();
        return VerificationCode.builder()
                .id(id)
                .user(user)
                .verificationCode(codeValue)
                .status(VerificationCodeStatus.ACTIVE)
                .attempts(0)
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(now.plusMinutes(5))
                .build();
    }

    @Test
    void generateAndSaveCode_ShouldGenerateAndSaveNewCode() {
        User user = createTestUser(1L);
        String expectedCode = "1234";
        
        when(verificationCodeRepository.findLastActiveCodeByUserId(user.getId()))
            .thenReturn(Optional.empty());
        when(verificationCodeRepository.save(any(VerificationCode.class)))
            .thenAnswer(invocation -> {
                VerificationCode code = invocation.getArgument(0);
                code.setId(1L);
                code.setVerificationCode(expectedCode);
                return code;
            });

        String result = verificationService.generateAndSaveCode(user);

        assertThat(result).isNotNull().hasSize(4);
        verify(verificationCodeRepository).save(any(VerificationCode.class));
        verify(verificationCodeCacheService).saveVerificationCode(any());
    }

    @Test
    void verifyCode_ValidCode_ShouldNotThrowException() {
        User user = createTestUser(1L);
        String inputCode = "1234";
        VerificationCode code = createTestCode(1L, user, inputCode);
        VerificationCodeCacheEntity cacheEntity = new VerificationCodeCacheEntity();
        VerificationCodeCacheEntity updatedCacheEntity = new VerificationCodeCacheEntity();

        when(verificationCodeCacheService.findCodeById(user.getId()))
                .thenReturn(Optional.of(cacheEntity));

        when(verificationCodeCacheMapper.toVerificationCode(cacheEntity))
                .thenReturn(code);

        when(verificationCodeCacheMapper.toVerificationCodeCacheEntity(code))
                .thenReturn(updatedCacheEntity);

        assertThatNoException()
                .isThrownBy(() -> verificationService.verifyCode(user, inputCode));

        verify(verificationCodeRepository).updateStatus(
                eq(code.getId()),
                eq(VerificationCodeStatus.USED),
                any(LocalDateTime.class));

        verify(verificationCodeCacheService).saveVerificationCode(updatedCacheEntity);
    }

    @Test
    void verifyCode_InvalidCode_ShouldThrowInvalidVerificationCodeException() {
        User user = createTestUser(1L);
        VerificationCode code = createTestCode(1L, user, "1234");
        VerificationCodeCacheEntity cacheEntity = new VerificationCodeCacheEntity();

        when(verificationCodeCacheService.findCodeById(user.getId()))
                .thenReturn(Optional.of(cacheEntity));
        when(verificationCodeCacheMapper.toVerificationCode(cacheEntity))
                .thenReturn(code);

        assertThatThrownBy(() -> verificationService.verifyCode(user, "wrong"))
                .isInstanceOf(InvalidVerificationCodeException.class);

        verify(verificationCodeRepository).incrementAttempts(
                eq(code.getId()),
                any(LocalDateTime.class));
    }

    @Test
    void verifyCode_ExpiredCode_ShouldThrowVerificationCodeExpiredException() {
        User user = createTestUser(1L);
        VerificationCode code = createTestCode(1L, user, "1234");
        code.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        VerificationCodeCacheEntity cacheEntity = new VerificationCodeCacheEntity();
        VerificationCodeCacheEntity updatedCacheEntity = new VerificationCodeCacheEntity();

        when(verificationCodeCacheService.findCodeById(user.getId()))
                .thenReturn(Optional.of(cacheEntity));
        when(verificationCodeCacheMapper.toVerificationCode(cacheEntity))
                .thenReturn(code);
        when(verificationCodeCacheMapper.toVerificationCodeCacheEntity(code))
                .thenReturn(updatedCacheEntity);

        assertThatThrownBy(() -> verificationService.verifyCode(user, "1234"))
                .isInstanceOf(VerificationCodeExpiredException.class);

        verify(verificationCodeRepository).updateStatus(
                eq(code.getId()),
                eq(VerificationCodeStatus.EXPIRED),
                any(LocalDateTime.class));
        verify(verificationCodeCacheService).saveVerificationCode(updatedCacheEntity);
    }

    @Test
    void verifyCode_NoActiveCode_ShouldThrowInvalidVerificationCodeException() {
        User user = createTestUser(1L);

        when(verificationCodeCacheService.findCodeById(user.getId()))
            .thenReturn(Optional.empty());
        when(verificationCodeRepository.findLastActiveCodeByUserId(user.getId()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificationService.verifyCode(user, "1234"))
            .isInstanceOf(InvalidVerificationCodeException.class)
            .hasMessageContaining("No active verification code found");
    }

    @Test
    void generateAndSaveCode_WithPreviousAttempts_ShouldIncrementAttempts() {
        User user = createTestUser(1L);
        VerificationCode previousCode = createTestCode(1L, user, "1111");
        previousCode.setAttempts(2);

        when(verificationCodeRepository.findLastActiveCodeByUserId(user.getId()))
            .thenReturn(Optional.of(previousCode));
        when(verificationCodeRepository.save(any(VerificationCode.class)))
            .thenAnswer(invocation -> {
                VerificationCode code = invocation.getArgument(0);
                code.setId(2L);
                return code;
            });

        verificationService.generateAndSaveCode(user);

        verify(verificationCodeRepository).save(argThat(code -> 
            code.getAttempts() == 3));
    }
}