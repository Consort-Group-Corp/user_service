package uz.consortgroup.userservice.service.mintrud;

import uz.consortgroup.core.api.v1.dto.mintrud.JobPositionResult;

public interface MintrudIntegrationService {
    JobPositionResult getJobInfo(String pinfl);
}
