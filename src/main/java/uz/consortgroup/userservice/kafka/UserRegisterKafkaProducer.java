package uz.consortgroup.userservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserRegisterKafkaProducer {
    private final KafkaTemplate kafkaTemplate;

    public void send(List<Object> messages) {
        try {
            log.info("Sending messages to Kafka: {}", messages.size());
            messages.forEach(message -> kafkaTemplate.sendDefault(message));
        } catch (Exception ex) {
            log.error("Error sending messages to Kafka: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
