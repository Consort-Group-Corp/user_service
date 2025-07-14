package uz.consortgroup.userservice.service.forum_group;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.core.api.v1.dto.forum.CreateForumGroupByHrRequest;
import uz.consortgroup.core.api.v1.dto.forum.HrForumGroupCreateResponse;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.entity.ForumUserGroup;
import uz.consortgroup.userservice.event.hr.HrActionType;
import uz.consortgroup.userservice.mapper.ForumGroupMapper;
import uz.consortgroup.userservice.service.event.course_group.CourseGroupEventService;
import uz.consortgroup.userservice.service.event.hr.HrActionLogger;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;
import uz.consortgroup.userservice.validator.UserServiceValidator;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HrForumGroupServiceImpl implements HrForumGroupService {

    private final CourseFeignClient courseFeignClient;
    private final ForumUserGroupService forumUserGroupService;
    private final ForumUserGroupMembershipService forumUserGroupMembershipService;
    private final HrActionLogger hrActionLogger;
    private final ForumGroupMapper forumGroupMapper;
    private final CourseGroupEventService courseGroupEventService;
    private final UserServiceValidator userServiceValidator;

    @Override
    @Transactional
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public HrForumGroupCreateResponse createHrForumGroup(CreateForumGroupByHrRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        UUID hrId = userDetails.getId();

        userServiceValidator.validateAllUsersExist(request.getUserIds());
        CourseResponseDto course = courseFeignClient.getCourseById(request.getCourseId());

        ForumUserGroup group = forumUserGroupService.create(request.getCourseId(), "HR: " + getCourseTitle(course));
        hrActionLogger.logHrCreatedForum(group.getId(), hrId, HrActionType.FORUM_GROUP_CREATED);

        forumUserGroupMembershipService.assignUsers(group.getId(), request.getUserIds());

        courseGroupEventService.sendCourseGroupEvent(course);

        request.getUserIds().forEach(userId ->
                hrActionLogger.logHrCreatedForum(userId, hrId, HrActionType.ADD_USER_TO_FORUM_GROUP)
        );

        return forumGroupMapper.toResponseDto(group);
    }

    private String getCourseTitle(CourseResponseDto course) {
        return course.getTranslations().isEmpty() ? "Unnamed" : course.getTranslations().getFirst().getTitle();
    }
}
