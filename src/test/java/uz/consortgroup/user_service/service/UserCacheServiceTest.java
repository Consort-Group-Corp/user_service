package uz.consortgroup.user_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.user_service.entity.cacheEntity.UserCacheEntity;
import uz.consortgroup.user_service.exception.UserNotFoundException;
import uz.consortgroup.user_service.repository.UserRedisRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserCacheServiceTest {
    @Mock
    private UserRedisRepository userRedisRepository;

    @InjectMocks
    private UserCacheService userCacheService;

    @Test
    void findUserById_Success() {
        Long userId = 1L;

        when(userRedisRepository.findById(userId)).thenReturn(Optional.of(new UserCacheEntity()));
        Optional<UserCacheEntity> result = userCacheService.findUserById(userId);
        assertTrue(result.isPresent());

        verify(userRedisRepository, times(1)).findById(userId);
    }

    @Test
    void findUserById_Fail() {
        Long userId = -1L;

        when(userRedisRepository.findById(userId)).thenThrow(new UserNotFoundException("User not found"));

        Optional<UserCacheEntity> result = userCacheService.findUserById(userId);
        assertTrue(result.isEmpty());

        verify(userRedisRepository, times(1)).findById(userId);
    }

    @Test
    void cacheUser_Success() {
        UserCacheEntity user = new UserCacheEntity();
        user.setId(1L);

        when(userRedisRepository.save(user)).thenReturn(user);

        userCacheService.cacheUser(user);

        verify(userRedisRepository, times(1)).save(user);
    }


    @Test
    void cacheUser_Fail_SaveException() {
        UserCacheEntity user = new UserCacheEntity();
        user.setId(1L);

        doThrow(new RuntimeException("Redis save failed")).when(userRedisRepository).save(user);

        try {
            userCacheService.cacheUser(user);
        } catch (RuntimeException e) {
            assertEquals("Failed to cache user: 1", e.getMessage());
        }

        verify(userRedisRepository, times(1)).save(user);
    }

    @Test
    void cacheUser_NullUser() {
        userCacheService.cacheUser(null);

        verify(userRedisRepository, times(0)).save(any(UserCacheEntity.class));
    }

    @Test
    void cacheUser_NullUserId() {
        UserCacheEntity user = new UserCacheEntity();
        user.setId(null);

        userCacheService.cacheUser(user);

        verify(userRedisRepository, times(0)).save(user);
    }

    @Test
    void cacheUsers_Success() {
        UserCacheEntity user1 = new UserCacheEntity();
        user1.setId(1L);
        UserCacheEntity user2 = new UserCacheEntity();
        user2.setId(2L);
        List<UserCacheEntity> users = Arrays.asList(user1, user2);

        when(userRedisRepository.save(user1)).thenReturn(user1);
        when(userRedisRepository.save(user2)).thenReturn(user2);

        userCacheService.cacheUsers(users);

        verify(userRedisRepository, times(1)).save(user1);
        verify(userRedisRepository, times(1)).save(user2);
    }

    @Test
    void cacheUsers_Failure() {
        UserCacheEntity user1 = new UserCacheEntity();
        user1.setId(1L);
        UserCacheEntity user2 = new UserCacheEntity();
        user2.setId(2L);
        List<UserCacheEntity> users = Arrays.asList(user1, user2);

        when(userRedisRepository.save(user1)).thenThrow(new RuntimeException("Redis error"));

        Exception exception = assertThrows(RuntimeException.class, () -> userCacheService.cacheUsers(users));
        assertEquals("Failed to cache users: 1", exception.getMessage());

        verify(userRedisRepository, times(1)).save(user1);
        verify(userRedisRepository, never()).save(user2);
    }

    @Test
    void removeUserFromCache_Success() {
        Long userId = 1L;

        doNothing().when(userRedisRepository).deleteById(userId);

        userCacheService.removeUserFromCache(userId);

        verify(userRedisRepository, times(1)).deleteById(userId);
    }

    @Test
    void removeUserFromCache_Failure() {
        Long userId = 1L;

        doThrow(new RuntimeException("Redis error")).when(userRedisRepository).deleteById(userId);

        Exception exception = assertThrows(RuntimeException.class, () -> userCacheService.removeUserFromCache(userId));
        assertEquals("Failed to remove user from cache: 1", exception.getMessage());

        verify(userRedisRepository, times(1)).deleteById(userId);
    }
}
