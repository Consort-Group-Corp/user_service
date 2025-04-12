package uz.consortgroup.userservice.service.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uz.consortgroup.userservice.event.PasswordResetRequestedEvent;
import uz.consortgroup.userservice.kafka.PasswordResetProducer;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordEventServiceTest {

    @Mock
    private PasswordResetProducer passwordResetProducer;

    @InjectMocks
    private PasswordEventService passwordEventService;

    @Test
    void sendPasswordEvent_ShouldSendEventWithCorrectData() {
        String email = "test@example.com";
        String token = "test-token";
        String expectedLink = "http://localhost?token=test-token";

        ReflectionTestUtils.setField(passwordEventService, "link", "http://localhost");

        passwordEventService.sendPasswordEvent(email, token);

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

        passwordEventService.sendPasswordEvent("email1@test.com", "token1");
        passwordEventService.sendPasswordEvent("email2@test.com", "token2");

        verify(passwordResetProducer, times(2)).sendPasswordRequestEvents(anyList());
    }
}