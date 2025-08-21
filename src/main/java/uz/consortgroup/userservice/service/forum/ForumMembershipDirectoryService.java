package uz.consortgroup.userservice.service.forum;

import java.util.List;
import java.util.UUID;

public interface ForumMembershipDirectoryService {
    List<UUID> getGroupIdsForUser(UUID userId);
}