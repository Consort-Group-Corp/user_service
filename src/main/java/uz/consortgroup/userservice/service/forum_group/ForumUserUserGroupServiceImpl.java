package uz.consortgroup.userservice.service.forum_group;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.enumeration.ForumAccessType;
import uz.consortgroup.userservice.entity.ForumUserGroup;
import uz.consortgroup.userservice.repository.ForumUserGroupRepository;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForumUserUserGroupServiceImpl implements ForumUserGroupService {

    private final ForumUserGroupRepository forumUserGroupRepository;

    @Override
    @Transactional
    public ForumUserGroup create(UUID courseId, String title, UUID ownerId, ForumAccessType forumAccessType) {
        log.info("Creating forum user group for courseId={}, title='{}'", courseId, title);

        ForumUserGroup group = ForumUserGroup.builder()
                .courseId(courseId)
                .ownerId(ownerId)
                .title(title)
                .forumAccessType(ForumAccessType.OPEN)
                .createdAt(Instant.now())
                .build();

        try {
            ForumUserGroup savedGroup = forumUserGroupRepository.save(group);
            log.info("Forum user group created with ID: {}", savedGroup.getId());
            return savedGroup;
        } catch (Exception e) {
            log.error("Failed to create forum user group for courseId={}, title='{}'", courseId, title, e);
            throw new RuntimeException("Failed to create forum user group", e);
        }
    }
}
