package uz.consortgroup.userservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.topic.KafkaTopic;

import java.util.List;

@Slf4j
@Component
public class CourseGroupProducer extends AbstractProducer {
    private final KafkaTopic kafkaTopic;

    public CourseGroupProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopic kafkaTopic) {
        super(kafkaTemplate);
        this.kafkaTopic = kafkaTopic;
    }

    public void sendCourseGroupEvents(List<Object> messages) {
        log.info("Sending {} messages to Kafka topic '{}'", messages.size(), getTopic());
        sendEventToTopic(getTopic(), messages);
    }

    @Override
    protected String getTopic() {
        return kafkaTopic.getCourseGroupTopic();
    }
}
