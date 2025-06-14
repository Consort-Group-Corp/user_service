package uz.consortgroup.userservice.config.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${kafka.bootstrap-servers}")
    private String servers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        props.put(JsonSerializer.TYPE_MAPPINGS,
                "user_registered:uz.consortgroup.userservice.event.user.UserRegisteredEvent," +
                "verification_code_resent:uz.consortgroup.userservice.event.user.VerificationCodeResentEvent," +
                        "user_profile_update:uz.consortgroup.userservice.event.user.UserProfileUpdateEvent," +
                        "password_reset_requested:uz.consortgroup.userservice.event.user.PasswordResetRequestedEvent," +
                        "super-admin-action:uz.consortgroup.userservice.event.admin.SuperAdminUserActionEvent," +
                        "mentor-action:uz.consortgroup.userservice.event.mentor.MentorResourceActionEvent," +
                        "course-group:uz.consortgroup.userservice.event.course_group.CourseGroupOpenedEvent," +
                        "hr-action:uz.consortgroup.userservice.event.hr.HrActionEvent");

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean("kafkaTemplate")
    public KafkaTemplate<String, Object> kafkaTemplate(@Qualifier("producerFactory") ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
