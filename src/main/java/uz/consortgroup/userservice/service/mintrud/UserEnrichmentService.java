package uz.consortgroup.userservice.service.mintrud;

import uz.consortgroup.core.api.v1.dto.mintrud.JobPositionResult;
import uz.consortgroup.userservice.entity.User;

public interface UserEnrichmentService {
    boolean enrichUserFromMehnat(User user, JobPositionResult result);
}
