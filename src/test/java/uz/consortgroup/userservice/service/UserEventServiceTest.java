package uz.consortgroup.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.enumeration.UserRole;
import uz.consortgroup.userservice.entity.enumeration.UserStatus;
import uz.consortgroup.userservice.kafka.UserRegisterKafkaProducer;
import uz.consortgroup.userservice.kafka.VerificationCodeResendProducer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserEventServiceTest {
    @Mock
    private UserRegisterKafkaProducer userRegisterKafkaProducer;

    @Mock
    private VerificationCodeResendProducer verificationCodeResendProducer;

    @InjectMocks
    private UserEventService userEventService;


    private User user;

    @BeforeEach
    void setUp() {
         user = User.builder()
                .id(1L)
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .workPlace("Google")
                .email("Ivan@gmail.com")
                .position("Developer")
                .pinfl("1234567890")
                .role(UserRole.STUDENT)
                .status(UserStatus.PENDING)
                .isVerified(false)
                .build();
    }


    @Test
    void sendRegistrationEvent_Success() {
        String verificationCode = "1234";
        userEventService.sendRegistrationEvent(user, verificationCode);

        verify(userRegisterKafkaProducer, times(1)).sendUserRegisterEvents(anyList());
    }

    @Test
    void resendVerificationCodeEvent_Success() {
       String newVerificationCode = "1234";
       userEventService.resendVerificationCodeEvent(user, newVerificationCode);

       verify(verificationCodeResendProducer, times(1)).sendVerificationCodeResendEvents(anyList());
    }

    @Test
    void sendRegistrationEvent_Failure() {
        String verificationCode = "1234";
        doThrow(new RuntimeException("Kafka send error")).when(userRegisterKafkaProducer).sendUserRegisterEvents(anyList());

        assertThrows(RuntimeException.class, () -> userEventService.sendRegistrationEvent(user, verificationCode));

        verify(userRegisterKafkaProducer, times(1)).sendUserRegisterEvents(anyList());
    }

    @Test
    void resendVerificationCodeEvent_Failure() {
        String newVerificationCode = "1234";
        doThrow(new RuntimeException("Kafka send error")).when(verificationCodeResendProducer).sendVerificationCodeResendEvents(anyList());

        assertThrows(RuntimeException.class, () -> userEventService.resendVerificationCodeEvent(user, newVerificationCode));

        verify(verificationCodeResendProducer, times(1)).sendVerificationCodeResendEvents(anyList());
    }
}
