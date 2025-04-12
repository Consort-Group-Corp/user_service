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
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VerificationCodeResendProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Mock
    private KafkaTopic kafkaTopic;
    
    @InjectMocks
    private VerificationCodeResendProducer producer;

    @Test
    void sendVerificationCodeResendEvents_ShouldSendAllMessages() {
        String topic = "verification-code-resend-topic";
        List<Object> messages = List.of("msg1", "msg2");
        
        when(kafkaTopic.getVerificationCodeResentTopic()).thenReturn(topic);
        
        producer.sendVerificationCodeResendEvents(messages);
        
        verify(kafkaTemplate).send(topic, "msg1");
        verify(kafkaTemplate).send(topic, "msg2");
    }

    @Test
    void sendVerificationCodeResendEvents_ShouldNotSendWhenEmptyList() {
        String topic = "verification-code-resend-topic";
        
        when(kafkaTopic.getVerificationCodeResentTopic()).thenReturn(topic);
        
        producer.sendVerificationCodeResendEvents(List.of());
        
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void getTopic_ShouldReturnCorrectTopic() {
        String expectedTopic = "verification-code-resend-topic";
        
        when(kafkaTopic.getVerificationCodeResentTopic()).thenReturn(expectedTopic);
        
        String actualTopic = producer.getTopic();
        
        assertThat(actualTopic).isEqualTo(expectedTopic);
    }

    @Test
    void sendVerificationCodeResendEvents_ShouldSendCorrectNumberOfMessages() {
        String topic = "verification-code-resend-topic";
        List<Object> messages = List.of("msg1", "msg2", "msg3");
        
        when(kafkaTopic.getVerificationCodeResentTopic()).thenReturn(topic);
        
        producer.sendVerificationCodeResendEvents(messages);
        
        verify(kafkaTemplate, times(3)).send(eq(topic), any());
    }
}