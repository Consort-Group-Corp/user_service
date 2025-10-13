package uz.consortgroup.userservice.service.access;

import uz.consortgroup.core.api.v1.dto.user.response.CourseAccessResponse;

import java.util.UUID;

public interface CourseAccessService {
    CourseAccessResponse checkAccess(UUID userId, UUID courseId);
}
