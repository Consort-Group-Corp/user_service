package uz.consortgroup.userservice.event.coursepurchased;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePurchasedEvent {
    private UUID messageId;
    private UUID userId;
    private UUID courseId;
    private Instant purchasedAt;
}