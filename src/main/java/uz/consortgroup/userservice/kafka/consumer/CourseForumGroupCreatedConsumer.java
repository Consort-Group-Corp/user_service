package uz.consortgroup.userservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.event.course_group.CourseForumGroupCreatedEvent;
import uz.consortgroup.userservice.service.processor.CourseForumGroupCreationProcessor;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseForumGroupCreatedConsumer extends AbstractKafkaConsumer<CourseForumGroupCreatedEvent> {
    private final CourseForumGroupCreationProcessor courseForumGroupCreationProcessor;

    @Override
    protected void handleMessage(CourseForumGroupCreatedEvent message) {
         log.info("Received batch of CourseForumGroupCreatedEven {}", message);
         courseForumGroupCreationProcessor.process(List.of(message));
    }

    @KafkaListener(
            topics = "${kafka.course-forum-group}",
            groupId = "${kafka.consumer-group-id}",
            containerFactory = "universalKafkaListenerContainerFactory"
    )
    public void onMessage(List<CourseForumGroupCreatedEvent> events, Acknowledgment ack) {
        log.info("Received batch of CoursePurchasedEvent: {}", events.size());
        processBatch(events, ack);
    }

    @Override
    protected UUID messageId(CourseForumGroupCreatedEvent message) {
        return message.getMessageId();
    }
}
