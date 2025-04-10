package uz.consortgroup.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;
import uz.consortgroup.userservice.repository.UserRedisRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCacheServiceTest {

    @Mock
    private UserRedisRepository userRedisRepository;

    @InjectMocks
    private UserCacheService userCacheService;

    private final UUID testUserId = UUID.randomUUID();

    private UserCacheEntity buildUserCacheEntity(UUID id) {
        return UserCacheEntity.builder()
                .id(id)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void findUserById_Success() {
        UserCacheEntity user = buildUserCacheEntity(testUserId);

        when(userRedisRepository.findById(testUserId)).thenReturn(Optional.of(user));

        Optional<UserCacheEntity> result = userCacheService.findUserById(testUserId);

        assertTrue(result.isPresent());
        assertEquals(testUserId, result.get().getId());
        verify(userRedisRepository).findById(testUserId);
    }

    @Test
    void findUserById_NotFound() {
        when(userRedisRepository.findById(testUserId)).thenReturn(Optional.empty());

        Optional<UserCacheEntity> result = userCacheService.findUserById(testUserId);

        assertTrue(result.isEmpty());
        verify(userRedisRepository).findById(testUserId);
    }

    @Test
    void cacheUser_Success() {
        UserCacheEntity user = buildUserCacheEntity(testUserId);

        userCacheService.cacheUser(user);

        verify(userRedisRepository).save(user);
    }

    @Test
    void cacheUser_Failure() {
        UserCacheEntity user = buildUserCacheEntity(testUserId);

        doThrow(new RuntimeException("Redis error")).when(userRedisRepository).save(user);

        Exception exception = assertThrows(RuntimeException.class,
                () -> userCacheService.cacheUser(user));

        assertEquals("Failed to cache user: " + testUserId, exception.getMessage());
        verify(userRedisRepository).save(user);
    }

    @Test
    void cacheUser_NullUser() {
        userCacheService.cacheUser(null);

        verify(userRedisRepository, never()).save(any());
    }

    @Test
    void cacheUser_NullId() {
        UserCacheEntity user = UserCacheEntity.builder().build();

        userCacheService.cacheUser(user);

        verify(userRedisRepository, never()).save(any());
    }

    @Test
    void cacheUsers_Success() {
        UserCacheEntity user1 = buildUserCacheEntity(testUserId);
        UserCacheEntity user2 = buildUserCacheEntity(UUID.randomUUID());
        List<UserCacheEntity> users = List.of(user1, user2);

        userCacheService.cacheUsers(users);

        verify(userRedisRepository).save(user1);
        verify(userRedisRepository).save(user2);
    }

    @Test
    void cacheUsers_PartialFailure() {
        UserCacheEntity user1 = buildUserCacheEntity(testUserId);
        UserCacheEntity user2 = buildUserCacheEntity(UUID.randomUUID());
        List<UserCacheEntity> users = List.of(user1, user2);

        doThrow(new RuntimeException("Redis error")).when(userRedisRepository).save(user1);

        Exception exception = assertThrows(RuntimeException.class,
                () -> userCacheService.cacheUsers(users));

        assertEquals("Failed to cache users: " + testUserId, exception.getMessage());
        verify(userRedisRepository).save(user1);
        verify(userRedisRepository, never()).save(user2);
    }

    @Test
    void removeUserFromCache_Success() {
        userCacheService.removeUserFromCache(testUserId);

        verify(userRedisRepository).deleteById(testUserId);
    }

    @Test
    void removeUserFromCache_Failure() {
        doThrow(new RuntimeException("Redis error"))
                .when(userRedisRepository).deleteById(testUserId);

        Exception exception = assertThrows(RuntimeException.class,
                () -> userCacheService.removeUserFromCache(testUserId));

        assertEquals("Failed to remove user from cache: " + testUserId, exception.getMessage());
        verify(userRedisRepository).deleteById(testUserId);
    }
}