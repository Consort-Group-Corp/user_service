package uz.consortgroup.userservice.service.event.course_group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.userservice.entity.ForumUserGroup;
import uz.consortgroup.userservice.event.course_group.CourseGroupOpenedEvent;
import uz.consortgroup.userservice.kafka.CourseGroupProducer;
import uz.consortgroup.userservice.service.forum_group.ForumUserGroupService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseGroupEventService {
   private final CourseGroupProducer courseGroupProducer;
   private final ForumUserGroupService forumUserGroupService;

   public void sendCourseGroupEvent(CourseResponseDto course) {
      String title = getCourseTitle(course);

      ForumUserGroup group = forumUserGroupService.create(course.getId(), title);

      CourseGroupOpenedEvent event = CourseGroupOpenedEvent.builder()
              .messageId(UUID.randomUUID())
              .courseId(course.getId())
              .courseTitle(getCourseTitle(course))
              .authorId(course.getAuthorId())
              .startTime(course.getStartTime())
              .endTime(course.getEndTime())
              .groupId(group.getId())
              .createdAt(group.getCreatedAt())
              .build();

      courseGroupProducer.sendCourseGroupEvents(List.of(event));
   }

   private String getCourseTitle(CourseResponseDto course) {
      return course.getTranslations().isEmpty() ? "Unnamed" : course.getTranslations().getFirst().getTitle();
   }
}
