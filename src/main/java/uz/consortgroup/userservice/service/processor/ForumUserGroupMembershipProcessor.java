package uz.consortgroup.userservice.service.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;
import uz.consortgroup.userservice.service.forum_group.ForumUserGroupMembershipService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumUserGroupMembershipProcessor implements ActionProcessor<CoursePurchasedEvent> {
    private final ForumUserGroupMembershipService forumUserGroupMembershipService;

    @Override
    public void process(List<CoursePurchasedEvent> events) {
        forumUserGroupMembershipService.assignUsersToForumGroups(events);
    }
}
