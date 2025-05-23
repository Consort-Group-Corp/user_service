package uz.consortgroup.userservice.service.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Language;
import uz.consortgroup.userservice.event.user.PasswordResetRequestedEvent;
import uz.consortgroup.userservice.kafka.PasswordResetProducer;
import uz.consortgroup.userservice.service.event.user.PasswordEventService;

import java.util.UUID;

import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordEventServiceTest {

    @Mock
    private PasswordResetProducer passwordResetProducer;

    @InjectMocks
    private PasswordEventService passwordEventService;

    @Test
    void sendPasswordEvent_ShouldSendEventWithCorrectData() {
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        String token = "test-token";
        String expectedLink = "http://localhost?token=test-token";

        ReflectionTestUtils.setField(passwordEventService, "link", "http://localhost");

        passwordEventService.sendPasswordEvent(email, userId, token, Language.ENGLISH);

        verify(passwordResetProducer).sendPasswordRequestEvents(argThat(events -> {
            if (events.size() != 1) return false;
            PasswordResetRequestedEvent event = (PasswordResetRequestedEvent) events.get(0);
            return event.getEmail().equals(email)
                    && event.getToken().equals(token)
                    && event.getResetLink().equals(expectedLink)
                    && event.getEventType() != null;
        }));
    }

    @Test
    void sendPasswordEvent_ShouldGenerateUniqueMessageIds() {

        passwordEventService.sendPasswordEvent("email1@test.com", UUID.randomUUID(),"token1", Language.ENGLISH);
        passwordEventService.sendPasswordEvent("email2@test.com", UUID.randomUUID(), "token2", Language.ENGLISH);

        verify(passwordResetProducer, times(2)).sendPasswordRequestEvents(anyList());
    }
}