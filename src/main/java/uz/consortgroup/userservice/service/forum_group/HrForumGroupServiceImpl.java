package uz.consortgroup.userservice.service.forum_group;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.core.api.v1.dto.forum.CreateForumGroupByHrRequest;
import uz.consortgroup.core.api.v1.dto.forum.HrForumGroupCreateResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.ForumAccessType;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.entity.ForumUserGroup;
import uz.consortgroup.userservice.event.hr.HrActionType;
import uz.consortgroup.userservice.mapper.ForumGroupMapper;
import uz.consortgroup.userservice.service.event.course_group.CourseGroupEventService;
import uz.consortgroup.userservice.service.event.hr.HrActionLogger;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;
import uz.consortgroup.userservice.validator.UserServiceValidator;

import java.util.UUID;

@Slf4j
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
    public HrForumGroupCreateResponse createHrForumGroup(CreateForumGroupByHrRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        UUID hrId = userDetails.getId();

        log.info("HR forum group creation started by HR: {}. CourseId: {}, Users count: {}",
                hrId, request.getCourseId(), request.getUserIds().size());

        try {
            userServiceValidator.validateAllUsersExist(request.getUserIds());
            userServiceValidator.validateIsMentor(request.getOwnerId());
            log.info("User existence validated for {} user(s)", request.getUserIds().size());

            CourseResponseDto course = courseFeignClient.getCourseById(request.getCourseId());
            String title = getCourseTitle(course);
            log.info("Fetched course: id={}, title={}", course.getId(), title);

            ForumUserGroup group = forumUserGroupService.create(
                    request.getCourseId(),
                    "HR: " + title,
                    request.getOwnerId(),
                    ForumAccessType.OPEN);
            log.info("Forum group created with id: {}", group.getId());

            hrActionLogger.logHrCreatedForum(group.getId(), hrId, HrActionType.FORUM_GROUP_CREATED);
            log.info("HR forum group creation action logged");

            forumUserGroupMembershipService.assignUsers(group.getId(), request.getUserIds());
            log.info("Assigned {} users to forum group {}", request.getUserIds().size(), group.getId());

            courseGroupEventService.sendCourseGroupEvent(group, request.getStartTime(), request.getEndTime(), request.getOwnerId());
            log.info("Course group event sent for courseId={}", course.getId());

            request.getUserIds().forEach(userId ->
                    hrActionLogger.logHrCreatedForum(userId, hrId, HrActionType.ADD_USER_TO_FORUM_GROUP)
            );
            log.info("HR add-user actions logged for each user");

            HrForumGroupCreateResponse response = forumGroupMapper.toResponseDto(group);
            log.info("Returning HR forum group response with groupId={}", response.getGroupId());
            return response;

        } catch (Exception ex) {
            log.error("Error occurred during HR forum group creation. HR: {}, CourseId: {}", hrId, request.getCourseId(), ex);
            throw ex;
        }
    }

    private String getCourseTitle(CourseResponseDto course) {
        return course.getTranslations().isEmpty() ? "Unnamed" : course.getTranslations().getFirst().getTitle();
    }
}
