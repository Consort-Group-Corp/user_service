package uz.consortgroup.userservice.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.event.course_group.CourseForumGroupCreatedEvent;
import uz.consortgroup.userservice.service.forum_group.CourseForumGroupCreationService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseForumGroupCreationProcessor implements ActionProcessor<CourseForumGroupCreatedEvent> {

    private final CourseForumGroupCreationService courseForumGroupCreationService;

    @Override
    public void process(List<CourseForumGroupCreatedEvent> events) {
        log.info("Processing {} CourseForumGroupCreatedEvent(s)", events.size());
        try {
            courseForumGroupCreationService.saveAllForumGroupCreations(events);
            log.debug("Successfully processed {} CourseForumGroupCreatedEvent(s)", events.size());
        } catch (Exception e) {
            log.error("Failed to process CourseForumGroupCreatedEvents", e);
            throw e;
        }
    }
}
