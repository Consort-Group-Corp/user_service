package uz.consortgroup.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.entity.cacheEntity.VerificationCodeCacheEntity;
import uz.consortgroup.userservice.entity.enumeration.VerificationCodeStatus;
import uz.consortgroup.userservice.repository.VerificationCodeRedisRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationCodeCacheServiceTest {
    @Mock
    private VerificationCodeRedisRepository verificationCodeRedisRepository;

    @InjectMocks
    private VerificationCodeCacheService verificationCodeCacheService;

    private VerificationCodeCacheEntity createTestCode(Long id, Long userId, String code) {
        return VerificationCodeCacheEntity.builder()
                .id(id)
                .userId(userId)
                .verificationCode(code)
                .status(VerificationCodeStatus.ACTIVE)
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findCodeById_ExistingCode_ShouldReturnCode() {
        Long codeId = 1L;
        Long userId = 1L;
        VerificationCodeCacheEntity code = createTestCode(codeId, userId, "ABCD");

        when(verificationCodeRedisRepository.findById(codeId))
                .thenReturn(Optional.of(code));

        Optional<VerificationCodeCacheEntity> result = verificationCodeCacheService.findCodeById(codeId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(codeId);
        assertThat(result.get().getUserId()).isEqualTo(userId);
        verify(verificationCodeRedisRepository).findById(codeId);
    }

    @Test
    void findCodeById_NonExistingCode_ShouldReturnEmpty() {
        Long codeId = 1L;
        when(verificationCodeRedisRepository.findById(codeId))
                .thenReturn(Optional.empty());

        Optional<VerificationCodeCacheEntity> result = verificationCodeCacheService.findCodeById(codeId);

        assertThat(result).isEmpty();
        verify(verificationCodeRedisRepository).findById(codeId);
    }

    @Test
    void saveVerificationCode_ValidCode_ShouldSaveSuccessfully() {
        Long userId = 1L;
        Long codeId = 1L;
        VerificationCodeCacheEntity code = createTestCode(codeId, userId, "ABCD");

        verificationCodeCacheService.saveVerificationCode(code);

        verify(verificationCodeRedisRepository).save(code);
    }

    @Test
    void saveVerificationCodes_ValidCodes_ShouldSaveSuccessfully() {
        Long userId1 = 1L;
        Long userId2 = 2L;
        List<VerificationCodeCacheEntity> codes = List.of(
                createTestCode(1L, userId1, "CODE1"),
                createTestCode(2L, userId2, "CODE2")
        );

        verificationCodeCacheService.saveVerificationCodes(codes);

        verify(verificationCodeRedisRepository).saveAll(codes);
    }

    @Test
    void saveVerificationCode_RepositoryThrowsException_ShouldThrowRuntimeException() {
        Long userId = 1L;
        VerificationCodeCacheEntity code = createTestCode(1L, userId, "ABCD");

        when(verificationCodeRedisRepository.save(code))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> verificationCodeCacheService.saveVerificationCode(code))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to cache verification code");

        verify(verificationCodeRedisRepository).save(code);
    }

}