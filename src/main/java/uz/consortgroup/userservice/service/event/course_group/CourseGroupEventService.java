package uz.consortgroup.userservice.service.event.course_group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.entity.ForumUserGroup;
import uz.consortgroup.userservice.event.course_group.CourseGroupOpenedEvent;
import uz.consortgroup.userservice.kafka.CourseGroupProducer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseGroupEventService {
   private final CourseGroupProducer courseGroupProducer;
   private final CourseFeignClient courseFeignClient;

   public void sendCourseGroupEvent(ForumUserGroup group, Instant startTime, Instant endTime, UUID authorId) {
      CourseResponseDto course = courseFeignClient.getCourseById(group.getCourseId());

      CourseGroupOpenedEvent event = CourseGroupOpenedEvent.builder()
              .messageId(UUID.randomUUID())
              .courseId(group.getCourseId())
              .courseTitle(course.getTranslations().isEmpty()
                      ? "Unnamed" : course.getTranslations().getFirst().getTitle())
              .ownerId(authorId)
              .forumAccessType(group.getForumAccessType())
              .startTime(startTime)
              .endTime(endTime)
              .groupId(group.getId())
              .createdAt(group.getCreatedAt())
              .build();

      courseGroupProducer.sendCourseGroupEvents(List.of(event));
   }
}

