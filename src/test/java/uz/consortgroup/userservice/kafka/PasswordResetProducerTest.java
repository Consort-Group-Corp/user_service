package uz.consortgroup.userservice.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import uz.consortgroup.userservice.topic.KafkaTopic;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopic kafkaTopic;

    @InjectMocks
    private PasswordResetProducer passwordResetProducer;

    @Test
    void sendPasswordRequestEvents_ShouldSendMessagesIndividually() {
        String topicName = "password-reset-topic";
        List<Object> messages = List.of("message1", "message2");

        when(kafkaTopic.getPasswordResetRequestTopic()).thenReturn(topicName);

        passwordResetProducer.sendPasswordRequestEvents(messages);

        verify(kafkaTemplate).send(topicName, "message1");
        verify(kafkaTemplate).send(topicName, "message2");
    }

    @Test
    void getTopic_ShouldReturnPasswordResetTopic() {
        String expectedTopic = "password-reset-topic";
        when(kafkaTopic.getPasswordResetRequestTopic()).thenReturn(expectedTopic);

        String actualTopic = passwordResetProducer.getTopic();

        assertThat(actualTopic).isEqualTo(expectedTopic);
    }

    @Test
    void sendPasswordRequestEvents_ShouldHandleEmptyList() {
        String topicName = "password-reset-topic";
        List<Object> messages = List.of();

        when(kafkaTopic.getPasswordResetRequestTopic()).thenReturn(topicName);

        passwordResetProducer.sendPasswordRequestEvents(messages);

        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void sendPasswordRequestEvents_ShouldHandleNullList() {
        String topicName = "password-reset-topic";

        assertThatThrownBy(() -> passwordResetProducer.sendPasswordRequestEvents(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void sendPasswordRequestEvents_ShouldHandleKafkaError() {
        String topicName = "password-reset-topic";
        List<Object> messages = List.of("message1");

        when(kafkaTopic.getPasswordResetRequestTopic()).thenReturn(topicName);
        when(kafkaTemplate.send(topicName, "message1"))
                .thenThrow(new RuntimeException("Kafka error"));

        assertThatThrownBy(() -> passwordResetProducer.sendPasswordRequestEvents(messages))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Kafka error");
    }
}