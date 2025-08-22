package uz.consortgroup.userservice.event.course_group;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.core.api.v1.dto.user.enumeration.ForumAccessType;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseGroupOpenedEvent {
    private UUID messageId;
    private UUID courseId;
    private UUID ownerId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ForumAccessType forumAccessType;
    private String courseTitle;
    private UUID authorId;
    private Instant startTime;
    private Instant endTime;
    private Instant createdAt;
    private UUID groupId;
}
