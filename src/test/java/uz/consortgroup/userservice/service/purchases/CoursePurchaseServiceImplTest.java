package uz.consortgroup.userservice.service.purchases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import uz.consortgroup.userservice.entity.UserPurchasedCourse;
import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;
import uz.consortgroup.userservice.repository.UserPurchasedCourseRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoursePurchaseServiceImplTest {

    @Mock
    private UserPurchasedCourseRepository userPurchasedCourseRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CoursePurchaseServiceImpl coursePurchaseService;

    @Test
    void saveAllPurchasedCourses_Success() {
        UUID messageId = UUID.randomUUID();
        CoursePurchasedEvent event = createTestEvent(messageId);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        
        coursePurchaseService.saveAllPurchasedCourses(List.of(event));
        
        verify(userPurchasedCourseRepository).saveAll(anyList());
    }

    @Test
    void saveAllPurchasedCourses_EmptyList() {
        coursePurchaseService.saveAllPurchasedCourses(List.of());
        verify(userPurchasedCourseRepository, never()).saveAll(any());
    }

    @Test
    void saveAllPurchasedCourses_DuplicateEvent() {
        UUID messageId = UUID.randomUUID();
        CoursePurchasedEvent event = createTestEvent(messageId);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(false);
        
        coursePurchaseService.saveAllPurchasedCourses(List.of(event));
        
        verify(userPurchasedCourseRepository, times(1)).saveAll(any());
    }

    @Test
    void saveAllPurchasedCourses_RepositoryException() {
        UUID messageId = UUID.randomUUID();
        CoursePurchasedEvent event = createTestEvent(messageId);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        when(userPurchasedCourseRepository.saveAll(any())).thenThrow(new RuntimeException("DB error"));
        
        assertThrows(RuntimeException.class, () -> 
            coursePurchaseService.saveAllPurchasedCourses(List.of(event)));
    }

    @Test
    void hasActiveAccess_Active() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UserPurchasedCourse purchase = UserPurchasedCourse.builder()
            .accessUntil(Instant.now().plus(1, ChronoUnit.DAYS))
            .build();
        
        when(userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId))
            .thenReturn(Optional.of(purchase));
        
        assertTrue(coursePurchaseService.hasActiveAccess(userId, courseId));
    }

    @Test
    void hasActiveAccess_Expired() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UserPurchasedCourse purchase = UserPurchasedCourse.builder()
            .accessUntil(Instant.now().minus(1, ChronoUnit.DAYS))
            .build();
        
        when(userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId))
            .thenReturn(Optional.of(purchase));
        
        assertFalse(coursePurchaseService.hasActiveAccess(userId, courseId));
    }

    @Test
    void hasActiveAccess_NotFound() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        
        when(userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId))
            .thenReturn(Optional.empty());
        
        assertFalse(coursePurchaseService.hasActiveAccess(userId, courseId));
    }

    private CoursePurchasedEvent createTestEvent(UUID messageId) {
        return CoursePurchasedEvent.builder()
            .messageId(messageId)
            .userId(UUID.randomUUID())
            .courseId(UUID.randomUUID())
            .purchasedAt(Instant.now())
            .accessUntil(Instant.now().plus(30, ChronoUnit.DAYS))
            .build();
    }
}