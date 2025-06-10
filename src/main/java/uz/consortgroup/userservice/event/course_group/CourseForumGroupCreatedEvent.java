package uz.consortgroup.userservice.event.course_group;

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
public class CourseForumGroupCreatedEvent {
    private UUID messageId;
    private UUID courseId;
    private UUID groupId;
    private Instant startTime;
    private Instant endTime;
    private Instant createdAt;
}
