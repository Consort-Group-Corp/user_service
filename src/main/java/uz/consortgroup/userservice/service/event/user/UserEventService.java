package uz.consortgroup.userservice.service.event.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.request.UserProfileRequestDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.event.user.EventType;
import uz.consortgroup.userservice.event.user.UserProfileUpdateEvent;
import uz.consortgroup.userservice.event.user.UserRegisteredEvent;
import uz.consortgroup.userservice.event.user.VerificationCodeResentEvent;
import uz.consortgroup.userservice.kafka.UserRegisteredProducer;
import uz.consortgroup.userservice.kafka.UserUpdateProfileProducer;
import uz.consortgroup.userservice.kafka.VerificationCodeResendProducer;

import java.util.List;
import java.util.UUID;

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

    public void sendUserUpdateProfileEvent(UUID userId, UserProfileRequestDto userProfileRequestDto) {
        UserProfileUpdateEvent event = UserProfileUpdateEvent.builder()
                .messageId(UUID.randomUUID())
                .userId(userId)
                .lastName(userProfileRequestDto.getLastName())
                .firstName(userProfileRequestDto.getFirstName())
                .middleName(userProfileRequestDto.getMiddleName())
                .bornDate(userProfileRequestDto.getBornDate())
                .phoneNumber(userProfileRequestDto.getPhoneNumber())
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
