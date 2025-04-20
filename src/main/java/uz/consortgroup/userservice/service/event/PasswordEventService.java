package uz.consortgroup.userservice.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.entity.enumeration.Language;
import uz.consortgroup.userservice.event.EventType;
import uz.consortgroup.userservice.event.PasswordResetRequestedEvent;
import uz.consortgroup.userservice.kafka.PasswordResetProducer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class PasswordEventService {
    private final PasswordResetProducer passwordResetProducer;
    @Value("${app.link}")
    private String link;

    public void sendPasswordEvent(String email, UUID userId, String token, Language language) {
        String resetLink = generateResetLink(token);

        PasswordResetRequestedEvent event = PasswordResetRequestedEvent.builder()
                .messageId(UUID.randomUUID())
                .userId(userId)
                .email(email)
                .token(token)
                .resetLink(resetLink)
                .language(language)
                .eventType(EventType.PASSWORD_RESET_REQUESTED)
                .build();

        passwordResetProducer.sendPasswordRequestEvents(List.of(event));
    }

    private String generateResetLink(String resetToken) {
        return link + "?token=" + resetToken;
    }
}
