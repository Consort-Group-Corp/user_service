package uz.consortgroup.userservice.entity.cacheEntity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import uz.consortgroup.userservice.entity.enumeration.VerificationCodeStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@RedisHash(value = "verification_codes_from_cache", timeToLive = 300)
public class VerificationCodeCacheEntity implements Serializable {
    @Id
    private Long id;

    private Long userId;
    private String verificationCode;
    private VerificationCodeStatus status;
    private int attempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime usedAt;
    private LocalDateTime expiresAt;
}
