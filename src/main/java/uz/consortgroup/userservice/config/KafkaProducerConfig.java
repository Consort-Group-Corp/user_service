package uz.consortgroup.userservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import uz.consortgroup.userservice.kafka.UserRegisterKafkaProducer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${kafka.bootstrap-servers}")
    private String servers;

    @Value("${kafka.user-registration}")
    private String userRegistrationTopic;

    @Bean
    public ProducerFactory<String, Object> userRegistrationProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean("userRegistrationKafkaTemplate")
    public KafkaTemplate<String, Object> userRegistrationKafkaTemplate(@Qualifier("userRegistrationProducerFactory") ProducerFactory<String, Object> userRegistrationProducerFactory) {
        return new KafkaTemplate<>(userRegistrationProducerFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.kafka.producer.enabled", havingValue = "true", matchIfMissing = true)
    public UserRegisterKafkaProducer userProducer(@Qualifier("userRegistrationKafkaTemplate") KafkaTemplate<String, Object> template) {
        template.setDefaultTopic(userRegistrationTopic);
        return new UserRegisterKafkaProducer(template);
    }
}
