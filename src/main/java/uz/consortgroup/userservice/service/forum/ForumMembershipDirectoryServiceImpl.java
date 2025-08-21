package uz.consortgroup.userservice.service.forum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.repository.ForumUserGroupMembershipRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumMembershipDirectoryServiceImpl implements ForumMembershipDirectoryService {
    private final ForumUserGroupMembershipRepository forumUserGroupMembershipRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getGroupIdsForUser(UUID userId) {
        log.debug("Fetching group IDs for user: {}", userId);
        List<UUID> groupIds = forumUserGroupMembershipRepository.findGroupIdsByUserId(userId);
        log.debug("Found {} groups for user: {}", groupIds.size(), userId);
        return groupIds;
    }
}