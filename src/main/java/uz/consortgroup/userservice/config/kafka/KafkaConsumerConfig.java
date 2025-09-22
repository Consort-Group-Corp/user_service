package uz.consortgroup.userservice.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConsumerConfig {
    @Value("${kafka.consumer-group-id}")
    private String groupId;

    @Value("${kafka.bootstrap-servers}")
    private String servers;

    @Value("${kafka.session-timeout-ms}")
    private String sessionTimeoutMs;

    @Value("${kafka.max-partition-fetch-bytes}")
    private String maxPartitionFetchBytes;

    @Value("${kafka.max-poll-records}")
    private String maxPollRecords;

    @Value("${kafka.max-poll-interval-ms}")
    private String maxPollIntervalMs;

    @Bean
    public ConsumerFactory<String, Object> universalConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "uz.consortgroup.userservice.event");

        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);

        props.put(JsonSerializer.TYPE_MAPPINGS, "course-purchased:uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> universalKafkaListenerContainerFactory(
            @Qualifier("universalConsumerFactory") ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.setConcurrency(3);

        ContainerProperties containerProps = factory.getContainerProperties();
        containerProps.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        containerProps.setPollTimeout(5000);
        containerProps.setMissingTopicsFatal(false);

        factory.setRecordFilterStrategy(record -> {
            if (record.value() == null) {
                log.warn("Received null value in topic {}", record.topic());
                return true;
            }
            return false;
        });

        factory.setCommonErrorHandler(kafkaErrorHandler());

        return factory;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                (record, exception) -> {
                    log.error("Failed to process message from topic {}: {}",
                            record.topic(), exception.getMessage());
                },
                new FixedBackOff(1000, 3)
        );

        handler.addNotRetryableExceptions(
                NullPointerException.class,
                IllegalArgumentException.class,
                DeserializationException.class
        );

        handler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("Retrying message (attempt {}): {}", deliveryAttempt, ex.getMessage())
        );

        return handler;
    }
}
