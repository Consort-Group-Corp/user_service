package uz.consortgroup.userservice.validator.strategy;

import uz.consortgroup.core.api.v1.dto.user.response.EligibilityResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.EligibilityReason;
import java.util.UUID;

public abstract class AbstractEligibilityStrategy implements EligibilityStrategy {
    private final EligibilityReason reason;
    
    protected AbstractEligibilityStrategy(EligibilityReason reason) {
        this.reason = reason;
    }
    
    @Override
    public EligibilityResponse check(UUID userId, UUID courseId) {
        return EligibilityResponse.builder()
                .eligible(false)
                .reason(reason)
                .accessUntil(null)
                .build();
    }
}