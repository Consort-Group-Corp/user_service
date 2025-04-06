package uz.consortgroup.user_service.entity.cacheEntity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import uz.consortgroup.user_service.entity.enumeration.UserStatus;
import uz.consortgroup.user_service.entity.enumeration.UserRole;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@RedisHash(value = "user_from_cache", timeToLive = 86400)
public class UserCacheEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;
    private String language;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate bornDate;
    private String phoneNumber;
    private String workPlace;
    private String email;
    private String position;
    private String pinfl;
    private Boolean isVerified;
    private String verificationCode;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
