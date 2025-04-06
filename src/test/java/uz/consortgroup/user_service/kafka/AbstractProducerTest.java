package uz.consortgroup.user_service.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AbstractProducerTest {
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private AbstractProducer producer;

    @BeforeEach
    void setUp() {
        producer = new AbstractProducer(kafkaTemplate) {
            @Override
            protected String getTopic() {
                return "test-topic";
            }
        };
    }

    @Test
    void sendEventToTopic_Success() {
        List<Object> messages = List.of("Message 1", "Message 2");

        producer.sendEventToTopic("test-topic", messages);

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate, times(2)).send(eq("test-topic"), messageCaptor.capture());

        List<Object> sentMessages = messageCaptor.getAllValues();
        assertEquals(messages, sentMessages);
    }

    @Test
    void sendEventToTopic_Fail() {
        List<Object> messages = List.of("Message 1");
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), any());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> producer.sendEventToTopic("test-topic", messages));
        assertEquals("Kafka error", thrown.getMessage());

        verify(kafkaTemplate, times(1)).send(eq("test-topic"), eq("Message 1"));
    }
}
