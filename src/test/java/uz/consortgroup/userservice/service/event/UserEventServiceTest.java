package uz.consortgroup.userservice.service.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.dto.UserProfileDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.enumeration.Language;
import uz.consortgroup.userservice.event.EventType;
import uz.consortgroup.userservice.event.UserProfileUpdateEvent;
import uz.consortgroup.userservice.event.UserRegisteredEvent;
import uz.consortgroup.userservice.event.VerificationCodeResentEvent;
import uz.consortgroup.userservice.kafka.UserRegisteredProducer;
import uz.consortgroup.userservice.kafka.UserUpdateProfileProducer;
import uz.consortgroup.userservice.kafka.VerificationCodeResendProducer;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventServiceTest {

    @Mock
    private UserUpdateProfileProducer userUpdateProfileProducer;

    @Mock
    private VerificationCodeResendProducer verificationCodeResendProducer;

    @Mock
    private UserRegisteredProducer userRegisteredProducer;

    @InjectMocks
    private UserEventService userEventService;

    @Test
    void sendUserRegisteredEvent_ShouldSendEvent() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setLanguage(Language.ENGLISH);
        user.setEmail("test@example.com");
        String verificationCode = "123456";

        userEventService.sendUserRegisteredEvent(user, verificationCode);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Object>> captor = ArgumentCaptor.forClass(List.class);
        verify(userRegisteredProducer).sendUserRegisteredEvents(captor.capture());

        List<Object> events = captor.getValue();
        assertEquals(1, events.size());

        UserRegisteredEvent event = (UserRegisteredEvent) events.get(0);
        assertEquals(user.getId(), event.getUserId());
        assertEquals(user.getLanguage(), event.getLanguage());
        assertEquals(user.getEmail(), event.getEmail());
        assertEquals(verificationCode, event.getVerificationCode());
        assertEquals(EventType.USER_REGISTERED, event.getEventType());
        assertNotNull(event.getMessageId());
    }

    @Test
    void sendUserUpdateProfileEvent_ShouldSendEvent() {
        UUID userId = UUID.randomUUID();
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setLastName("Doe");
        userProfileDto.setFirstName("John");
        userProfileDto.setMiddleName("Middle");
        userProfileDto.setBornDate(LocalDate.of(1990, 1, 1));
        userProfileDto.setPhoneNumber("+1234567890");

        userEventService.sendUserUpdateProfileEvent(userId, userProfileDto);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Object>> captor = ArgumentCaptor.forClass(List.class);
        verify(userUpdateProfileProducer).sendUserUpdateProfileEvents(captor.capture());

        List<Object> events = captor.getValue();
        assertEquals(1, events.size());

        UserProfileUpdateEvent event = (UserProfileUpdateEvent) events.get(0);
        assertEquals(userId, event.getUserId());
        assertEquals("Doe", event.getLastName());
        assertEquals("John", event.getFirstName());
        assertEquals("Middle", event.getMiddleName());
        assertEquals(LocalDate.of(1990, 1, 1), event.getBornDate());
        assertEquals("+1234567890", event.getPhoneNumber());
        assertEquals(EventType.USER_PROFILE_UPDATED, event.getEventType());
        assertNotNull(event.getMessageId());
    }

    @Test
    void resendVerificationCodeEvent_ShouldSendEvent() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setLanguage(Language.ENGLISH);
        user.setEmail("test@example.com");
        String verificationCode = "654321";

        userEventService.resendVerificationCodeEvent(user, verificationCode);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Object>> captor = ArgumentCaptor.forClass(List.class);
        verify(verificationCodeResendProducer).sendVerificationCodeResendEvents(captor.capture());

        List<Object> events = captor.getValue();
        assertEquals(1, events.size());

        VerificationCodeResentEvent event = (VerificationCodeResentEvent) events.get(0);
        assertEquals(user.getId(), event.getUserId());
        assertEquals(user.getLanguage(), event.getLanguage());
        assertEquals(user.getEmail(), event.getEmail());
        assertEquals(verificationCode, event.getNewVerificationCode());
        assertEquals(EventType.VERIFICATION_CODE_SENT, event.getEventType());
        assertNotNull(event.getMessageId());
    }

    @Test
    void sendMultipleEvents_ShouldSendEachSeparately() {
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setLanguage(Language.ENGLISH);
        user1.setEmail("test1@example.com");

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setLanguage(Language.ENGLISH);
        user2.setEmail("test2@example.com");

        userEventService.sendUserRegisteredEvent(user1, "111111");
        userEventService.sendUserRegisteredEvent(user2, "222222");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Object>> captor = ArgumentCaptor.forClass(List.class);
        verify(userRegisteredProducer, times(2)).sendUserRegisteredEvents(captor.capture());
        List<List<Object>> allCalls = captor.getAllValues();

        assertEquals(2, allCalls.size());

        UserRegisteredEvent event1 = (UserRegisteredEvent) allCalls.get(0).get(0);
        assertEquals(user1.getId(), event1.getUserId());
        assertEquals("111111", event1.getVerificationCode());

        UserRegisteredEvent event2 = (UserRegisteredEvent) allCalls.get(1).get(0);
        assertEquals(user2.getId(), event2.getUserId());
        assertEquals("222222", event2.getVerificationCode());
    }

    @Test
    void sendUserUpdateProfileEvent_WithNullFields_ShouldSendEvent() {
        UUID userId = UUID.randomUUID();
        UserProfileDto userProfileDto = new UserProfileDto();

        userEventService.sendUserUpdateProfileEvent(userId, userProfileDto);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Object>> captor = ArgumentCaptor.forClass(List.class);
        verify(userUpdateProfileProducer).sendUserUpdateProfileEvents(captor.capture());

        List<Object> events = captor.getValue();
        assertEquals(1, events.size());

        UserProfileUpdateEvent event = (UserProfileUpdateEvent) events.get(0);
        assertEquals(userId, event.getUserId());
        assertNull(event.getFirstName());
        assertNull(event.getLastName());
        assertNull(event.getMiddleName());
        assertNull(event.getPhoneNumber());
        assertNull(event.getBornDate());
        assertEquals(EventType.USER_PROFILE_UPDATED, event.getEventType());
        assertNotNull(event.getMessageId());
    }
}
