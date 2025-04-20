package uz.consortgroup.userservice.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.dto.UserProfileDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.event.EventType;
import uz.consortgroup.userservice.event.UserProfileUpdateEvent;
import uz.consortgroup.userservice.event.UserRegisteredEvent;
import uz.consortgroup.userservice.event.VerificationCodeResentEvent;
import uz.consortgroup.userservice.kafka.UserRegisteredProducer;
import uz.consortgroup.userservice.kafka.UserUpdateProfileProducer;
import uz.consortgroup.userservice.kafka.VerificationCodeResendProducer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserEventService {
    private final UserUpdateProfileProducer userUpdateProfileProducer;
    private final VerificationCodeResendProducer verificationCodeResendProducer;
    private final UserRegisteredProducer userRegisteredProducer;

    public void sendUserRegisteredEvent(User user, String verificationCode) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                        .messageId(UUID.randomUUID())
                        .userId(user.getId())
                        .language(user.getLanguage())
                        .email(user.getEmail())
                        .verificationCode(verificationCode)
                        .eventType(EventType.USER_REGISTERED)
                        .build();

        userRegisteredProducer.sendUserRegisteredEvents(List.of(event));
    }

    public void sendUserUpdateProfileEvent(UUID userId, UserProfileDto userProfileDto) {
        UserProfileUpdateEvent event = UserProfileUpdateEvent.builder()
                .messageId(UUID.randomUUID())
                .userId(userId)
                .lastName(userProfileDto.getLastName())
                .firstName(userProfileDto.getFirstName())
                .middleName(userProfileDto.getMiddleName())
                .bornDate(userProfileDto.getBornDate())
                .phoneNumber(userProfileDto.getPhoneNumber())
                .eventType(EventType.USER_PROFILE_UPDATED)
                .build();

        userUpdateProfileProducer.sendUserUpdateProfileEvents(List.of(event));
    }

    public void resendVerificationCodeEvent(User user, String verificationCode) {
        VerificationCodeResentEvent event = VerificationCodeResentEvent.builder()
                .messageId(UUID.randomUUID())
                .language(user.getLanguage())
                .userId(user.getId())
                .newVerificationCode(verificationCode)
                .email(user.getEmail())
                .eventType(EventType.VERIFICATION_CODE_SENT)
                .build();

        verificationCodeResendProducer.sendVerificationCodeResendEvents(List.of(event));
    }
}
