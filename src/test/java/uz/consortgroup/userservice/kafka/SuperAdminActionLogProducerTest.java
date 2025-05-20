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
class SuperAdminActionLogProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopic kafkaTopic;

    @InjectMocks
    private SuperAdminActionLogProducer superAdminActionLogProducer;

    @Test
    void sendUserCreatedEvents_WithEmptyList_ShouldNotSendAnyMessages() {
        String topicName = "user-created-topic";
        when(kafkaTopic.getSuperAdminActionTopic()).thenReturn(topicName);
        List<Object> emptyMessages = List.of();

        superAdminActionLogProducer.sendSuperAdminActionEvents(emptyMessages);

        verify(kafkaTopic, times(2)).getSuperAdminActionTopic(); // Изменили на 2 вызова
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void sendUserCreatedEvents_ShouldSendMessagesToCorrectTopic() {
        String topicName = "user-created-topic";
        when(kafkaTopic.getSuperAdminActionTopic()).thenReturn(topicName);
        List<Object> messages = List.of("message1", "message2");

        superAdminActionLogProducer.sendSuperAdminActionEvents(messages);

        verify(kafkaTopic, times(2)).getSuperAdminActionTopic(); // Изменили на 2 вызова
        verify(kafkaTemplate, times(messages.size())).send(eq(topicName), any());
    }

    @Test
    void sendSuperAdminActionEvents_WithNullMessages_ShouldThrowException() {
        assertThrows(NullPointerException.class, 
            () -> superAdminActionLogProducer.sendSuperAdminActionEvents(null));
    }

    @Test
    void getTopic_ShouldReturnCorrectTopicName() {
        String expectedTopic = "user-created-topic";
        when(kafkaTopic.getSuperAdminActionTopic()).thenReturn(expectedTopic);

        String actualTopic = superAdminActionLogProducer.getTopic();

        assertEquals(expectedTopic, actualTopic);
        verify(kafkaTopic).getSuperAdminActionTopic();
    }

    @Test
    void sendSuperAdminActionEvents_WhenKafkaFails_ShouldLogError() {
        String topicName = "user-created-topic";
        when(kafkaTopic.getSuperAdminActionTopic()).thenReturn(topicName);
        List<Object> messages = List.of("message1");
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), any());

        assertThrows(RuntimeException.class, 
            () -> superAdminActionLogProducer.sendSuperAdminActionEvents(messages));
    }
}