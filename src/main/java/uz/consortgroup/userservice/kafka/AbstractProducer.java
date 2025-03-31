package uz.consortgroup.userservice.kafka;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public abstract class AbstractProducer {
    private final KafkaTemplate kafkaTemplate;

    protected void sendEventToTopic(String topic, @NotNull @NotEmpty List<Object> messages) {
        try {
            messages.forEach(message -> {
                kafkaTemplate.send(topic, message);
            });

        } catch (Exception ex) {
            log.error("Error sending messages to Kafka topic '{}': {}", topic, ex.getMessage(), ex);
            throw ex;
        }
    }

    protected abstract String getTopic();
}
