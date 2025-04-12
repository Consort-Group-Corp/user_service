package uz.consortgroup.userservice.service.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

        UserRegisteredEvent expectedEvent = UserRegisteredEvent.builder()
                .messageId(1L)
                .userId(user.getId())
                .language(user.getLanguage())
                .email(user.getEmail())
                .verificationCode(verificationCode)
                .eventType(EventType.USER_REGISTERED)
                .build();

        verify(userRegisteredProducer).sendUserRegisteredEvents(List.of(expectedEvent));
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

        UserProfileUpdateEvent expectedEvent = UserProfileUpdateEvent.builder()
                .messageId(1L)
                .userId(userId)
                .lastName(userProfileDto.getLastName())
                .firstName(userProfileDto.getFirstName())
                .middleName(userProfileDto.getMiddleName())
                .bornDate(userProfileDto.getBornDate())
                .phoneNumber(userProfileDto.getPhoneNumber())
                .eventType(EventType.USER_PROFILE_UPDATED)
                .build();

        verify(userUpdateProfileProducer).sendUserUpdateProfileEvents(List.of(expectedEvent));
    }

    @Test
    void resendVerificationCodeEvent_ShouldSendEvent() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setLanguage(Language.ENGLISH);
        user.setEmail("test@example.com");
        String verificationCode = "654321";

        userEventService.resendVerificationCodeEvent(user, verificationCode);

        VerificationCodeResentEvent expectedEvent = VerificationCodeResentEvent.builder()
                .messageId(1L)
                .language(user.getLanguage())
                .userId(user.getId())
                .newVerificationCode(verificationCode)
                .email(user.getEmail())
                .eventType(EventType.VERIFICATION_CODE_SENT)
                .build();

        verify(verificationCodeResendProducer).sendVerificationCodeResendEvents(List.of(expectedEvent));
    }

    @Test
    void sendMultipleEvents_ShouldIncrementMessageId() {
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

        UserRegisteredEvent expectedEvent1 = UserRegisteredEvent.builder()
                .messageId(1L)
                .userId(user1.getId())
                .language(user1.getLanguage())
                .email(user1.getEmail())
                .verificationCode("111111")
                .eventType(EventType.USER_REGISTERED)
                .build();

        UserRegisteredEvent expectedEvent2 = UserRegisteredEvent.builder()
                .messageId(2L)
                .userId(user2.getId())
                .language(user2.getLanguage())
                .email(user2.getEmail())
                .verificationCode("222222")
                .eventType(EventType.USER_REGISTERED)
                .build();

        verify(userRegisteredProducer).sendUserRegisteredEvents(List.of(expectedEvent1));
        verify(userRegisteredProducer).sendUserRegisteredEvents(List.of(expectedEvent2));
    }

    @Test
    void sendUserUpdateProfileEvent_WithNullFields_ShouldSendEvent() {
        UUID userId = UUID.randomUUID();
        UserProfileDto userProfileDto = new UserProfileDto();

        userEventService.sendUserUpdateProfileEvent(userId, userProfileDto);

        UserProfileUpdateEvent expectedEvent = UserProfileUpdateEvent.builder()
                .messageId(1L)
                .userId(userId)
                .lastName(null)
                .firstName(null)
                .middleName(null)
                .bornDate(null)
                .phoneNumber(null)
                .eventType(EventType.USER_PROFILE_UPDATED)
                .build();

        verify(userUpdateProfileProducer).sendUserUpdateProfileEvents(List.of(expectedEvent));
    }
}