package uz.consortgroup.userservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;
import uz.consortgroup.userservice.service.processor.CoursePurchasedEventProcessor;
import uz.consortgroup.userservice.service.processor.ForumUserGroupMembershipProcessor;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoursePurchasedConsumer extends AbstractKafkaConsumer<CoursePurchasedEvent> {
    private final ForumUserGroupMembershipProcessor forumUserGroupMembershipProcessor;
    private final CoursePurchasedEventProcessor coursePurchasedEventProcessor;


    @KafkaListener(
            topics = "course-purchased-topic",
            groupId = "${kafka.consumer-group-id}",
            containerFactory = "universalKafkaListenerContainerFactory"
    )
    public void onMessage(List<CoursePurchasedEvent> events, Acknowledgment ack) {
        log.info("Received batch of CoursePurchasedEvent: {}", events.size());
        processBatch(events, ack);
    }

    @Override
    protected void handleMessage(CoursePurchasedEvent event) {
        log.info("Processing event: {}", event);
        coursePurchasedEventProcessor.process(List.of(event));
        forumUserGroupMembershipProcessor.process(List.of(event));
    }

    @Override
    protected UUID messageId(CoursePurchasedEvent event) {
        return event.getMessageId();
    }
}
