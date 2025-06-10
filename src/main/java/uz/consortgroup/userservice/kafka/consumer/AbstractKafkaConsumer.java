package uz.consortgroup.userservice.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public abstract class AbstractKafkaConsumer<T> {

    public void processBatch(List<T> messages, Acknowledgment ack) {
        List<CompletableFuture<Void>> futures = messages.stream()
                .filter(Objects::nonNull)
                .map(message -> CompletableFuture.runAsync(() -> {
                    try {
                        handleMessage(message);
                    } catch (Exception e) {
                        log.error("Error processing message: {}", message, e);
                    }
                }))
                .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allOf.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while waiting for all tasks to complete", e);
            Thread.currentThread().interrupt();
        } finally {
            ack.acknowledge();
        }
    }

    protected abstract void handleMessage(T message);
    protected abstract UUID messageId(T message);
}
