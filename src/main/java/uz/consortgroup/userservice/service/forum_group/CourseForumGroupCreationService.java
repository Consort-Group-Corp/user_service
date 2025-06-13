package uz.consortgroup.userservice.service.forum_group;

import uz.consortgroup.userservice.entity.CourseForumGroup;
import uz.consortgroup.userservice.event.course_group.CourseForumGroupCreatedEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseForumGroupCreationService {
    void saveAllForumGroupCreations(List<CourseForumGroupCreatedEvent> courseForumGroupCreatedEvents);
    Optional<CourseForumGroup> findByCourseId(UUID courseId);
    Optional<CourseForumGroup> findByGroupId(UUID groupId);
}
