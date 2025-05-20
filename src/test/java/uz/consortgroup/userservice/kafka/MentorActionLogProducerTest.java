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
class MentorActionLogProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopic kafkaTopic;

    @InjectMocks
    private MentorActionLogProducer producer;

    @Test
    void sendMentorActionEvents_ShouldSendMessagesIndividually() {
        String topicName = "mentor-action-topic";
        List<Object> messages = List.of("msg1", "msg2");

        when(kafkaTopic.getMentorActionTopic()).thenReturn(topicName);

        producer.sendMentorActionEvents(messages);

        verify(kafkaTemplate).send(topicName, "msg1");
        verify(kafkaTemplate).send(topicName, "msg2");
    }

    @Test
    void getTopic_ShouldReturnMentorActionTopic() {
        String expectedTopic = "mentor-action-topic";
        when(kafkaTopic.getMentorActionTopic()).thenReturn(expectedTopic);

        String actualTopic = producer.getTopic();

        assertThat(actualTopic).isEqualTo(expectedTopic);
    }

    @Test
    void sendMentorActionEvents_ShouldHandleEmptyList() {
        String topicName = "mentor-action-topic";
        List<Object> messages = List.of();

        when(kafkaTopic.getMentorActionTopic()).thenReturn(topicName);

        producer.sendMentorActionEvents(messages);

        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void sendMentorActionEvents_ShouldHandleNullList() {
        assertThatThrownBy(() -> producer.sendMentorActionEvents(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void sendMentorActionEvents_ShouldHandleKafkaError() {
        String topicName = "mentor-action-topic";
        List<Object> messages = List.of("msg1");

        when(kafkaTopic.getMentorActionTopic()).thenReturn(topicName);
        when(kafkaTemplate.send(topicName, "msg1"))
                .thenThrow(new RuntimeException("Kafka failure"));

        assertThatThrownBy(() -> producer.sendMentorActionEvents(messages))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Kafka failure");
    }
}