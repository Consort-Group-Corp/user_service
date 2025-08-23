package uz.consortgroup.userservice.validator.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.EligibilityReason;
import uz.consortgroup.core.api.v1.dto.user.response.EligibilityResponse;
import uz.consortgroup.userservice.service.operation.UserOperationsService;

import java.util.UUID;

@Component
@Slf4j
public class UserBlockedStrategy extends AbstractEligibilityStrategy {
    private final UserOperationsService userOperationsService;

    public UserBlockedStrategy(UserOperationsService userOperationsService) {
        super(EligibilityReason.USER_BLOCKED);
        this.userOperationsService = userOperationsService;
    }

    @Override
    public boolean isApplicable(UUID userId, UUID courseId) {
        boolean isBlocked = userOperationsService.isUserBlocked(userId);
        if (isBlocked) {
            log.warn("UserBlockedStrategy applicable: user {} is blocked", userId);
        }
        return isBlocked;
    }

    @Override
    public EligibilityResponse check(UUID userId, UUID courseId) {
        log.error("User {} cannot purchase course {} - user is blocked", userId, courseId);
        return super.check(userId, courseId);
    }
}