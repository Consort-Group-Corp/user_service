package uz.consortgroup.userservice.repository;

import io.micrometer.common.lang.Nullable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;

import java.time.LocalDateTime;
import java.util.Map;

@Repository
public interface UserRedisRepository extends CrudRepository<UserCacheEntity, Long> {
    @Query("""
        local key = 'user_from_cache:' .. ARGV[1]
        local current = redis.call('HGETALL', key)
        local fields = {
            'firstName', 'middleName', 'lastName', 
            'workPlace', 'email', 'position', 'pinfl'
        }
        local values = {
            ARGV[2], ARGV[3], ARGV[4], 
            ARGV[5], ARGV[6], ARGV[7], ARGV[8]
        }
        
        for i, field in ipairs(fields) do
            if values[i] ~= 'null' then
                redis.call('HSET', key, field, values[i])
            end
        end
        redis.call('HSET', key, 'updatedAt', ARGV[9])
        return redis.call('HGETALL', key)
    """)
    Map<String, String> partialUpdateUser(
            Long id,
            @Nullable String firstName,
            @Nullable String middleName,
            @Nullable String lastName,
            @Nullable String workPlace,
            @Nullable String email,
            @Nullable String position,
            @Nullable String pinfl,
            LocalDateTime updatedAt
    );

    @Query("""
    local key = 'user_from_cache:' .. ARGV[1]
    if ARGV[2] ~= 'null' then
        redis.call('HSET', key, 'verificationCode', ARGV[2])
    end
    if ARGV[3] ~= 'null' then
        redis.call('HSET', key, 'verificationCodeExpiredAt', ARGV[3])
    end
    if ARGV[4] ~= 'null' then
        redis.call('HSET', key, 'isVerified', ARGV[4])
    end
    if ARGV[5] ~= 'null' then
        redis.call('HSET', key, 'userStatus', ARGV[5])
    end
    redis.call('HSET', key, 'updatedAt', ARGV[6])
    return redis.call('HGETALL', key)
""")
    Map<String, String> partialUpdateVerification(
            Long userId,
            @Nullable String verificationCode,
            @Nullable String verificationCodeExpiredAt,
            @Nullable String isVerified,
            @Nullable String userStatus,
            LocalDateTime updatedAt
    );
}
