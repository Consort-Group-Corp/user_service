package uz.consortgroup.user_service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uz.consortgroup.user_service.topic.KafkaTopic;

import java.util.List;

@Slf4j
@Component

public class UserRegisterKafkaProducer extends AbstractProducer {
    private final KafkaTopic kafkaTopic;

    public UserRegisterKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopic kafkaTopic) {
        super(kafkaTemplate);
        this.kafkaTopic = kafkaTopic;
    }

    public void sendUserRegisterEvents(List<Object> messages) {
        log.info("Sending {} messages to Kafka topic '{}'", messages.size(), getTopic());
        sendEventToTopic(getTopic(), messages);
    }

    @Override
    protected String getTopic() {
        return kafkaTopic.getUserRegisteredTopic();
    }
}
