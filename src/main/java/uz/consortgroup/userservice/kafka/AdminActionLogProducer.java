package uz.consortgroup.userservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.topic.KafkaTopic;

import java.util.List;

@Component
@Slf4j
public class AdminActionLogProducer extends AbstractProducer {
    private final KafkaTopic kafkaTopic;

    public AdminActionLogProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopic kafkaTopic) {
        super(kafkaTemplate);
        this.kafkaTopic = kafkaTopic;
    }

    public void sendUserCreatedEvents(List<Object> messages) {
        log.info("Sending {} messages to Kafka topic '{}'", messages.size(), getTopic());
        sendEventToTopic(getTopic(), messages);
    }

    @Override
    protected String getTopic() {
        return kafkaTopic.getUserCreatedTopic();
    }
}
