package uz.consortgroup.userservice.service.forum_group;

import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;

import java.util.List;

public interface ForumUserGroupMembershipService {
    void assignUsersToForumGroups(List<CoursePurchasedEvent> events);
}
