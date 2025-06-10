package uz.consortgroup.userservice.service.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.event.course_group.CourseForumGroupCreatedEvent;
import uz.consortgroup.userservice.service.forum_group.CourseForumGroupCreationService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseForumGroupCreationProcessor implements ActionProcessor<CourseForumGroupCreatedEvent> {
    private final CourseForumGroupCreationService courseForumGroupCreationService;

    @Override
    public void process(List<CourseForumGroupCreatedEvent> events) {
        courseForumGroupCreationService.saveAllForumGroupCreations(events);
    }
}
