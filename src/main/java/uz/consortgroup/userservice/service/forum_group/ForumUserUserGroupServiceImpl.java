package uz.consortgroup.userservice.service.forum_group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.entity.ForumUserGroup;
import uz.consortgroup.userservice.repository.ForumUserGroupRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForumUserUserGroupServiceImpl implements ForumUserGroupService {
    private final ForumUserGroupRepository forumUserGroupRepository;

    @AllAspect
    @Transactional
    public ForumUserGroup create(UUID courseId, String title) {
        ForumUserGroup group = ForumUserGroup.builder()
                .courseId(courseId)
                .title(title)
                .createdAt(Instant.now())
                .build();

        return forumUserGroupRepository.save(group);
    }
}
