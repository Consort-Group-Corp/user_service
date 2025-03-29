package uz.consortgroup.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.entity.UserCacheEntity;
import uz.consortgroup.userservice.repository.UserRedisRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService {
    private final UserRedisRepository userRedisRepository;

    public Optional<UserCacheEntity> findUsersById(Long id) {
        return userRedisRepository.findById(id);
    }

    public void saveUsersToCache(UserCacheEntity user) {
        log.info("Saving user to Redis: {}", user);
        userRedisRepository.save(user);
    }

    public void saveUsersToCache(List<UserCacheEntity> users) {
        log.info("Saving batch of {} users to Redis", users.size());
        userRedisRepository.saveAll(users);
    }
}
