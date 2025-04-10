package uz.consortgroup.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.dto.UserProfileDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.enumeration.Language;
import uz.consortgroup.userservice.entity.enumeration.UserStatus;
import uz.consortgroup.userservice.kafka.UserRegisteredProducer;
import uz.consortgroup.userservice.kafka.UserUpdateProfileProducer;
import uz.consortgroup.userservice.kafka.VerificationCodeResendProducer;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

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

    private User user;
    private final UUID userId = UUID.randomUUID();
    private final String verificationCode = "123456";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .language(Language.UZBEK)
                .status(UserStatus.PENDING)
                .build();
    }

    @Test
    void sendUserRegisteredEvent_Success() {
        userEventService.sendUserRegisteredEvent(user, verificationCode);

        verify(userRegisteredProducer).sendUserRegisteredEvents(anyList());
    }

    @Test
    void sendUserUpdateProfileEvent_Success() {
        UserProfileDto profileDto = UserProfileDto.builder()
                .lastName("Smith")
                .firstName("John")
                .middleName("Michael")
                .bornDate(LocalDate.now())
                .phoneNumber("+998901234567")
                .build();

        userEventService.sendUserUpdateProfileEvent(userId, profileDto);

        verify(userUpdateProfileProducer).sendUserUpdateProfileEvents(anyList());
    }

    @Test
    void resendVerificationCodeEvent_Success() {
        userEventService.resendVerificationCodeEvent(user, verificationCode);

        verify(verificationCodeResendProducer).sendVerificationCodeResendEvents(anyList());
    }

    @Test
    void sendUserRegisteredEvent_ProducerFailure() {
        doThrow(new RuntimeException("Kafka error"))
                .when(userRegisteredProducer).sendUserRegisteredEvents(anyList());

        assertThrows(RuntimeException.class,
                () -> userEventService.sendUserRegisteredEvent(user, verificationCode));

        verify(userRegisteredProducer).sendUserRegisteredEvents(anyList());
    }

    @Test
    void sendUserUpdateProfileEvent_ProducerFailure() {
        UserProfileDto profileDto = UserProfileDto.builder().build();

        doThrow(new RuntimeException("Kafka error"))
                .when(userUpdateProfileProducer).sendUserUpdateProfileEvents(anyList());

        assertThrows(RuntimeException.class,
                () -> userEventService.sendUserUpdateProfileEvent(userId, profileDto));

        verify(userUpdateProfileProducer).sendUserUpdateProfileEvents(anyList());
    }

    @Test
    void resendVerificationCodeEvent_ProducerFailure() {
        doThrow(new RuntimeException("Kafka error"))
                .when(verificationCodeResendProducer).sendVerificationCodeResendEvents(anyList());

        assertThrows(RuntimeException.class,
                () -> userEventService.resendVerificationCodeEvent(user, verificationCode));

        verify(verificationCodeResendProducer).sendVerificationCodeResendEvents(anyList());
    }
}