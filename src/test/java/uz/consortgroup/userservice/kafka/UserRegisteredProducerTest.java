package uz.consortgroup.userservice.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import uz.consortgroup.userservice.topic.KafkaTopic;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegisteredProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopic kafkaTopic;

    @InjectMocks
    private UserRegisteredProducer userRegisteredProducer;

    @Test
    void sendUserRegisteredEvents_ShouldSendMessagesIndividually() {
        String topicName = "user-registration-topic";
        List<Object> messages = List.of("message1", "message2");

        when(kafkaTopic.getUserRegistrationTopic()).thenReturn(topicName);

        userRegisteredProducer.sendUserRegisteredEvents(messages);

        verify(kafkaTemplate).send(topicName, "message1");
        verify(kafkaTemplate).send(topicName, "message2");
    }

    @Test
    void getTopic_ShouldReturnUserRegistrationTopic() {
        String expectedTopic = "user-registration-topic";
        when(kafkaTopic.getUserRegistrationTopic()).thenReturn(expectedTopic);

        String actualTopic = userRegisteredProducer.getTopic();

        assertThat(actualTopic).isEqualTo(expectedTopic);
    }

    @Test
    void sendUserRegisteredEvents_ShouldHandleEmptyList() {
        String topicName = "user-registration-topic";
        List<Object> messages = List.of();

        when(kafkaTopic.getUserRegistrationTopic()).thenReturn(topicName);

        userRegisteredProducer.sendUserRegisteredEvents(messages);

        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void sendUserRegisteredEvents_ShouldHandleNullList() {
        assertThatThrownBy(() -> userRegisteredProducer.sendUserRegisteredEvents(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void sendUserRegisteredEvents_ShouldHandleKafkaError() {
        String topicName = "user-registration-topic";
        List<Object> messages = List.of("message1");

        when(kafkaTopic.getUserRegistrationTopic()).thenReturn(topicName);
        when(kafkaTemplate.send(topicName, "message1"))
                .thenThrow(new RuntimeException("Kafka error"));

        assertThatThrownBy(() -> userRegisteredProducer.sendUserRegisteredEvents(messages))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Kafka error");
    }

}