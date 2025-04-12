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
import uz.consortgroup.userservice.service.cache.VerificationCodeCacheService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private User createTestUser(UUID id) {
        return User.builder()
                .id(id)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .build();
    }

    private VerificationCode createTestCode(UUID id, User user, String codeValue) {
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
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId);
        String expectedCode = "1234";

        when(verificationCodeRepository.findLastActiveCodeByUserId(user.getId()))
                .thenReturn(Optional.empty());
        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenAnswer(invocation -> {
                    VerificationCode code = invocation.getArgument(0);
                    code.setId(UUID.randomUUID());
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
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId);
        String inputCode = "1234";
        VerificationCode code = createTestCode(UUID.randomUUID(), user, inputCode);
        VerificationCodeCacheEntity cacheEntity = new VerificationCodeCacheEntity();

        when(verificationCodeCacheService.findCodeById(user.getId()))
                .thenReturn(Optional.of(cacheEntity));
        when(verificationCodeCacheMapper.toVerificationCode(cacheEntity))
                .thenReturn(code);

        assertThatNoException()
                .isThrownBy(() -> verificationService.verifyCode(user, inputCode));

        verify(verificationCodeRepository).updateStatus(
                eq(code.getId()),
                eq(VerificationCodeStatus.USED),
                any(LocalDateTime.class));
    }

    @Test
    void verifyCode_InvalidCode_ShouldThrowInvalidVerificationCodeException() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId);
        VerificationCode code = createTestCode(UUID.randomUUID(), user, "1234");
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
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId);
        VerificationCode code = createTestCode(UUID.randomUUID(), user, "1234");
        code.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        VerificationCodeCacheEntity cacheEntity = new VerificationCodeCacheEntity();

        when(verificationCodeCacheService.findCodeById(user.getId()))
                .thenReturn(Optional.of(cacheEntity));
        when(verificationCodeCacheMapper.toVerificationCode(cacheEntity))
                .thenReturn(code);

        assertThatThrownBy(() -> verificationService.verifyCode(user, "1234"))
                .isInstanceOf(VerificationCodeExpiredException.class);

        verify(verificationCodeRepository).updateStatus(
                eq(code.getId()),
                eq(VerificationCodeStatus.EXPIRED),
                any(LocalDateTime.class));
    }

    @Test
    void verifyCode_NoActiveCode_ShouldThrowInvalidVerificationCodeException() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId);

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
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId);
        VerificationCode previousCode = createTestCode(UUID.randomUUID(), user, "1111");
        previousCode.setAttempts(2);

        when(verificationCodeRepository.findLastActiveCodeByUserId(user.getId()))
                .thenReturn(Optional.of(previousCode));
        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenAnswer(invocation -> {
                    VerificationCode code = invocation.getArgument(0);
                    code.setId(UUID.randomUUID());
                    return code;
                });

        verificationService.generateAndSaveCode(user);

        verify(verificationCodeRepository).save(argThat(code ->
                code.getAttempts() == 3));
    }
}