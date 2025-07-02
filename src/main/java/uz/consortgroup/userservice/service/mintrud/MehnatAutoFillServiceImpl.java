package uz.consortgroup.userservice.service.mintrud;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.mintrud.JobPositionResult;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class MehnatAutoFillServiceImpl implements MehnatAutoFillService {

    private final MintrudIntegrationService mintrudIntegrationService;
    private final UserEnrichmentService userEnrichmentService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    public void tryFetchDataFromMehnat(User user) {
        if (user.getPinfl() == null || Boolean.TRUE.equals(user.getMehnatDataFetched())) return;

        JobPositionResult result =mintrudIntegrationService.getJobInfo(user.getPinfl());
        if (result == null) return;

        boolean changed = userEnrichmentService.enrichUserFromMehnat(user, result);
        if(changed) {
            user.setMehnatDataFetched(true);
            userRepository.save(user);
        }
    }
}
