package uz.consortgroup.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.user_service.entity.User;
import uz.consortgroup.user_service.event.EventType;
import uz.consortgroup.user_service.event.UserRegistrationEvent;
import uz.consortgroup.user_service.event.VerificationCodeResentEvent;
import uz.consortgroup.user_service.kafka.UserRegisterKafkaProducer;
import uz.consortgroup.user_service.kafka.VerificationCodeResendProducer;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserEventService {
    private final UserRegisterKafkaProducer userRegisterKafkaProducer;
    private final VerificationCodeResendProducer verificationCodeResendProducer;
    private final AtomicLong messageIdGenerator = new AtomicLong(0);

    public void sendRegistrationEvent(User user, String verificationCode) {
        UserRegistrationEvent event = UserRegistrationEvent.builder()
                .messageId(messageIdGenerator.incrementAndGet())
                .language(user.getLanguage())
                .userId(user.getId())
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .email(user.getEmail())
                .verificationCode(verificationCode)
                .eventType(EventType.USER_REGISTERED)
                .build();

        userRegisterKafkaProducer.sendUserRegisterEvents(List.of(event));
    }

    public void resendVerificationCodeEvent(User user, String verificationCode) {
        VerificationCodeResentEvent event = VerificationCodeResentEvent.builder()
                .messageId(messageIdGenerator.incrementAndGet())
                .language(user.getLanguage())
                .userId(user.getId())
                .newVerificationCode(verificationCode)
                .email(user.getEmail())
                .eventType(EventType.VERIFICATION_CODE_SENT)
                .build();

        verificationCodeResendProducer.sendVerificationCodeResendEvents(List.of(event));
    }
}
