package uz.consortgroup.userservice.service.event.hr;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.event.hr.HrActionEvent;
import uz.consortgroup.userservice.event.hr.HrActionType;
import uz.consortgroup.userservice.kafka.HrActionLogProducer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HrActionLogger {
    private final HrActionLogProducer hrActionLogProducer;

    public void logHrAction(UUID userId, UUID hrId, HrActionType actionType) {
        HrActionEvent event = HrActionEvent.builder()
                .messageId(UUID.randomUUID())
                .userId(userId)
                .hrId(hrId)
                .hrActionType(actionType)
                .createdAt(LocalDateTime.now())
                .build();

        hrActionLogProducer.sendHrActionEvents(List.of(event));
    }
}
