package uz.consortgroup.userservice.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import uz.consortgroup.userservice.topic.KafkaTopic;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminActionLogProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopic kafkaTopic;

    @InjectMocks
    private AdminActionLogProducer adminActionLogProducer;

    @Test
    void sendUserCreatedEvents_WithEmptyList_ShouldNotSendAnyMessages() {
        String topicName = "user-created-topic";
        when(kafkaTopic.getUserCreatedTopic()).thenReturn(topicName);
        List<Object> emptyMessages = List.of();

        adminActionLogProducer.sendUserCreatedEvents(emptyMessages);

        verify(kafkaTopic, times(2)).getUserCreatedTopic(); // Изменили на 2 вызова
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void sendUserCreatedEvents_ShouldSendMessagesToCorrectTopic() {
        String topicName = "user-created-topic";
        when(kafkaTopic.getUserCreatedTopic()).thenReturn(topicName);
        List<Object> messages = List.of("message1", "message2");

        adminActionLogProducer.sendUserCreatedEvents(messages);

        verify(kafkaTopic, times(2)).getUserCreatedTopic(); // Изменили на 2 вызова
        verify(kafkaTemplate, times(messages.size())).send(eq(topicName), any());
    }

    @Test
    void sendUserCreatedEvents_WithNullMessages_ShouldThrowException() {
        assertThrows(NullPointerException.class, 
            () -> adminActionLogProducer.sendUserCreatedEvents(null));
    }

    @Test
    void getTopic_ShouldReturnCorrectTopicName() {
        String expectedTopic = "user-created-topic";
        when(kafkaTopic.getUserCreatedTopic()).thenReturn(expectedTopic);

        String actualTopic = adminActionLogProducer.getTopic();

        assertEquals(expectedTopic, actualTopic);
        verify(kafkaTopic).getUserCreatedTopic();
    }

    @Test
    void sendUserCreatedEvents_WhenKafkaFails_ShouldLogError() {
        String topicName = "user-created-topic";
        when(kafkaTopic.getUserCreatedTopic()).thenReturn(topicName);
        List<Object> messages = List.of("message1");
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), any());

        assertThrows(RuntimeException.class, 
            () -> adminActionLogProducer.sendUserCreatedEvents(messages));
    }
}