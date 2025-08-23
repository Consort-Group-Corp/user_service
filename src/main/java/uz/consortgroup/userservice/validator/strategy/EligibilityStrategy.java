package uz.consortgroup.userservice.validator.strategy;

import uz.consortgroup.core.api.v1.dto.user.response.EligibilityResponse;
import java.util.UUID;

public interface EligibilityStrategy {
    EligibilityResponse check(UUID userId, UUID courseId);
    boolean isApplicable(UUID userId, UUID courseId);
}