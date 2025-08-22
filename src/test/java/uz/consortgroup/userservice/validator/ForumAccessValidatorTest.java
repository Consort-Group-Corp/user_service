package uz.consortgroup.userservice.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.forum.enumeration.ForumAccessReason;
import uz.consortgroup.userservice.entity.CourseForumGroup;
import uz.consortgroup.userservice.repository.ForumUserGroupMembershipRepository;
import uz.consortgroup.userservice.service.forum_group.CourseForumGroupCreationService;
import uz.consortgroup.userservice.service.purchases.CoursePurchaseService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForumAccessValidatorTest {

    @Mock
    private CourseForumGroupCreationService courseForumGroupCreationService;

    @Mock
    private ForumUserGroupMembershipRepository forumUserGroupMembershipRepository;

    @Mock
    private CoursePurchaseService coursePurchaseService;

    @InjectMocks
    private ForumAccessValidator forumAccessValidator;

    private final UUID courseId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID groupId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        courseForumGroupCreationService = mock(CourseForumGroupCreationService.class);
        forumUserGroupMembershipRepository = mock(ForumUserGroupMembershipRepository.class);
        coursePurchaseService = mock(CoursePurchaseService.class);
        forumAccessValidator = new ForumAccessValidator(
                courseForumGroupCreationService,
                forumUserGroupMembershipRepository,
                coursePurchaseService
        );
    }

    @Test
    void validateAccess_ShouldReturnUserHasAccessByCourse() {
        when(courseForumGroupCreationService.findByCourseId(courseId))
                .thenReturn(Optional.of(CourseForumGroup.builder().groupId(groupId).build()));
        when(forumUserGroupMembershipRepository.existsByUserIdAndGroupId(userId, groupId)).thenReturn(true);
        when(coursePurchaseService.hasActiveAccess(userId, courseId)).thenReturn(true);

        ForumAccessReason result = forumAccessValidator.validateAccessByCourse(courseId, userId);
        assertEquals(ForumAccessReason.USER_HAS_ACCESS, result);
    }

    @Test
    void validateAccess_ShouldReturnAccessExpired_WhenUserInGroupButNoAccessByCourse() {
        when(courseForumGroupCreationService.findByCourseId(courseId))
                .thenReturn(Optional.of(CourseForumGroup.builder().groupId(groupId).build()));
        when(forumUserGroupMembershipRepository.existsByUserIdAndGroupId(userId, groupId)).thenReturn(true);
        when(coursePurchaseService.hasActiveAccess(userId, courseId)).thenReturn(false);

        ForumAccessReason result = forumAccessValidator.validateAccessByCourse(courseId, userId);
        assertEquals(ForumAccessReason.ACCESS_EXPIRED, result);
    }

    @Test
    void validateAccess_ByCourse_ShouldReturnUserNotInGroup() {
        when(courseForumGroupCreationService.findByCourseId(courseId))
                .thenReturn(Optional.of(CourseForumGroup.builder().groupId(groupId).build()));
        when(forumUserGroupMembershipRepository.existsByUserIdAndGroupId(userId, groupId)).thenReturn(false);

        ForumAccessReason result = forumAccessValidator.validateAccessByCourse(courseId, userId);
        assertEquals(ForumAccessReason.USER_NOT_IN_GROUP, result);
    }

    @Test
    void validateAccess_ByCourse_ShouldReturnForumGroupNotFound() {
        when(courseForumGroupCreationService.findByCourseId(courseId)).thenReturn(Optional.empty());

        ForumAccessReason result = forumAccessValidator.validateAccessByCourse(courseId, userId);
        assertEquals(ForumAccessReason.FORUM_GROUP_NOT_FOUND, result);
    }
}
