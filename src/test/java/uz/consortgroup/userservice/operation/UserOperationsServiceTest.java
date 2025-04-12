package uz.consortgroup.userservice.operation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.cache.UserCacheService;
import uz.consortgroup.userservice.service.operation.UserOperationsService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserOperationsServiceTest {

    @Mock
    private UserCacheService userCacheService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCacheMapper userCacheMapper;

    @InjectMocks
    private UserOperationsService userOperationsService;

    @Test
    void findUserById_ShouldReturnUserFromCache() {
        UUID userId = UUID.randomUUID();
        UserCacheEntity cachedUser = new UserCacheEntity();
        User expectedUser = new User();

        when(userCacheService.findUserById(userId)).thenReturn(Optional.of(cachedUser));
        when(userCacheMapper.toUserEntity(cachedUser)).thenReturn(expectedUser);

        User result = userOperationsService.findUserById(userId);

        assertEquals(expectedUser, result);
        verify(userCacheService).findUserById(userId);
        verify(userCacheMapper).toUserEntity(cachedUser);
        verifyNoInteractions(userRepository);
    }

    @Test
    void findUserById_ShouldReturnUserFromDbAndCacheWhenNotInCache() {
        UUID userId = UUID.randomUUID();
        User dbUser = new User();
        when(userCacheService.findUserById(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(dbUser));

        User result = userOperationsService.findUserById(userId);

        assertEquals(dbUser, result);
        verify(userCacheService).findUserById(userId);
        verify(userRepository).findById(userId);
        verify(userCacheService).cacheUser(any());
    }

    @Test
    void findUserById_ShouldThrowExceptionWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userCacheService.findUserById(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userOperationsService.findUserById(userId);
        });

        verify(userCacheService).findUserById(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserFromDbAndCache_ShouldReturnUserFromCache() {
        UUID userId = UUID.randomUUID();
        UserCacheEntity cachedUser = new UserCacheEntity();
        User user = new User();

        when(userCacheService.findUserById(userId)).thenReturn(Optional.of(cachedUser));
        when(userCacheMapper.toUserEntity(cachedUser)).thenReturn(user);

        User result = userOperationsService.getUserFromDbAndCache(userId);

        assertEquals(user, result);
        verify(userCacheService).findUserById(userId);
        verify(userCacheMapper).toUserEntity(cachedUser);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserFromDbAndCache_ShouldReturnUserFromDbAndCacheWhenNotInCache() {
        UUID userId = UUID.randomUUID();
        User dbUser = new User();
        when(userCacheService.findUserById(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(dbUser));

        User result = userOperationsService.getUserFromDbAndCache(userId);

        assertEquals(dbUser, result);
        verify(userCacheService).findUserById(userId);
        verify(userRepository).findById(userId);
        verify(userCacheService).cacheUser(any());
    }

    @Test
    void getUserFromDbAndCache_ShouldThrowExceptionWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userCacheService.findUserById(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userOperationsService.getUserFromDbAndCache(userId);
        });

        verify(userCacheService).findUserById(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    void cacheUser_ShouldCallCacheService() {
        User user = new User();
        userOperationsService.cacheUser(user);
        verify(userCacheService).cacheUser(any());
    }

    @Test
    void cacheUser_ShouldThrowExceptionForNullUser() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userOperationsService.cacheUser(null);
        });

        assertEquals("User cannot be null", exception.getMessage());
        verify(userCacheService, never()).cacheUser(any());
        verify(userCacheMapper, never()).toUserCache(any());
    }
}