package uz.consortgroup.userservice.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import uz.consortgroup.userservice.topic.KafkaTopic;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserUpdateProfileProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopic kafkaTopic;

    @InjectMocks
    private UserUpdateProfileProducer userUpdateProfileProducer;

    @Test
    void sendUserUpdateProfileEvents_ShouldSendMessages() {
        String topic = "user-update-profile-topic";
        List<Object> messages = List.of("msg1", "msg2");

        when(kafkaTopic.getUserUpdateProfileTopic()).thenReturn(topic);

        userUpdateProfileProducer.sendUserUpdateProfileEvents(messages);

        verify(kafkaTemplate).send(topic, "msg1");
        verify(kafkaTemplate).send(topic, "msg2");
    }

    @Test
    void sendUserUpdateProfileEvents_ShouldNotSendWhenEmptyList() {
        String topic = "user-update-profile-topic";
        List<Object> messages = List.of();

        when(kafkaTopic.getUserUpdateProfileTopic()).thenReturn(topic);

        userUpdateProfileProducer.sendUserUpdateProfileEvents(messages);

        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void getTopic_ShouldReturnCorrectTopic() {
        String expectedTopic = "user-update-profile-topic";
        when(kafkaTopic.getUserUpdateProfileTopic()).thenReturn(expectedTopic);

        String actualTopic = userUpdateProfileProducer.getTopic();

        assertThat(actualTopic).isEqualTo(expectedTopic);
    }

    @Test
    void sendUserUpdateProfileEvents_ShouldSendCorrectNumberOfMessages() {
        String topic = "user-update-profile-topic";
        List<Object> messages = List.of("msg1", "msg2", "msg3");

        when(kafkaTopic.getUserUpdateProfileTopic()).thenReturn(topic);

        userUpdateProfileProducer.sendUserUpdateProfileEvents(messages);

        verify(kafkaTemplate, times(3)).send(eq(topic), any());
    }
}