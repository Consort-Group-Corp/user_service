package uz.consortgroup.userservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ResetTokenStore {

    private static final String PREFIX = "pwdreset:jti:";

    private final StringRedisTemplate redis;

    public boolean consumeOnce(String jti, Duration ttl) {
        String key = PREFIX + jti;
        Boolean ok = redis.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }

    public boolean isConsumed(String jti) {
        String key = PREFIX + jti;
        Boolean exists = redis.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
