package uz.consortgroup.userservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.topic.KafkaTopic;

import java.util.List;

@Component
@Slf4j
public class SuperAdminActionLogProducer extends AbstractProducer {
    private final KafkaTopic kafkaTopic;

    public SuperAdminActionLogProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopic kafkaTopic) {
        super(kafkaTemplate);
        this.kafkaTopic = kafkaTopic;
    }

    public void sendSuperAdminActionEvents(List<Object> messages) {
        log.info("Sending {} messages to Kafka topic '{}'", messages.size(), getTopic());
        sendEventToTopic(getTopic(), messages);
    }

    @Override
    protected String getTopic() {
        return kafkaTopic.getSuperAdminActionTopic();
    }
}
