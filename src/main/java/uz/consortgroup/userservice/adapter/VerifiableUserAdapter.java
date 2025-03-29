package uz.consortgroup.userservice.adapter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.UserCacheEntity;

import java.time.LocalDateTime;

@Getter
@Slf4j
public class VerifiableUserAdapter {
    private final String verificationCode;
    private final LocalDateTime verificationCodeExpiredAt;
    private final Long id;

    public VerifiableUserAdapter(User user) {
        this.verificationCode = user.getVerificationCode();
        this.verificationCodeExpiredAt = user.getVerificationCodeExpiredAt();
        this.id = user.getId();
        log.debug("Created VerifiableUserAdapter from User with id: {}", id);
    }

    public VerifiableUserAdapter(UserCacheEntity userCacheEntity) {
        this.verificationCode = userCacheEntity.getVerificationCode();
        this.verificationCodeExpiredAt = userCacheEntity.getVerificationCodeExpiredAt();
        this.id = userCacheEntity.getId();
        log.debug("Created VerifiableUserAdapter from UserCacheEntity with id: {}", id);
    }
}
