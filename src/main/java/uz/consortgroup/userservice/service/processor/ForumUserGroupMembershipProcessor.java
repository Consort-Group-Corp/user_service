package uz.consortgroup.userservice.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;
import uz.consortgroup.userservice.service.forum_group.ForumUserGroupMembershipService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumUserGroupMembershipProcessor implements ActionProcessor<CoursePurchasedEvent> {

    private final ForumUserGroupMembershipService forumUserGroupMembershipService;

    @Override
    public void process(List<CoursePurchasedEvent> events) {
        log.info("Processing {} CoursePurchasedEvent(s) for forum group assignment", events.size());
        try {
            forumUserGroupMembershipService.assignUsersToForumGroups(events);
            log.debug("Successfully assigned users to forum groups for {} event(s)", events.size());
        } catch (Exception e) {
            log.error("Failed to assign users to forum groups", e);
            throw e;
        }
    }
}
