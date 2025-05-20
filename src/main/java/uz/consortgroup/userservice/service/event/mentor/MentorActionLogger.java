package uz.consortgroup.userservice.service.event.mentor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.event.mentor.MentorResourceActionEvent;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.kafka.MentorActionLogProducer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MentorActionLogger {
    private final MentorActionLogProducer mentorActionLogProducer;

    public void logMentorResourceAction (UUID resource, UUID mentorId, MentorActionType mentorActionType) {
        MentorResourceActionEvent event = MentorResourceActionEvent.builder()
                .messageId(UUID.randomUUID())
                .mentorId(mentorId)
                .resourceId(resource)
                .mentorActionType(mentorActionType)
                .createdAt(LocalDateTime.now())
                .build();

        mentorActionLogProducer.sendMentorActionEvents(List.of(event));
    }
}
