package uz.consortgroup.userservice.service.forum_group;

import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;

import java.util.List;
import java.util.UUID;

public interface ForumUserGroupMembershipService {
    void assignUsersToForumGroups(List<CoursePurchasedEvent> events);
    void assignUsers(UUID groupId, List<UUID> userIds);
}
