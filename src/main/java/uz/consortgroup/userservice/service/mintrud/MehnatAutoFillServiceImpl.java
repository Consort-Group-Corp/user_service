package uz.consortgroup.userservice.service.mintrud;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.mintrud.JobPositionResult;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MehnatAutoFillServiceImpl implements MehnatAutoFillService {

    private final MintrudIntegrationService mintrudIntegrationService;
    private final UserEnrichmentService userEnrichmentService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void tryFetchDataFromMehnat(User user) {
        log.info("Attempting to auto-fill user info from Mehnat API for userId={}, pinfl={}", user.getId(), user.getPinfl());

        if (user.getPinfl() == null) {
            log.warn("Skipping Mehnat data fetch — user has no PINFL: userId={}", user.getId());
            return;
        }

        if (Boolean.TRUE.equals(user.getMehnatDataFetched())) {
            log.info("Mehnat data already fetched previously for userId={}", user.getId());
            return;
        }

        JobPositionResult result = mintrudIntegrationService.getJobInfo(user.getPinfl());

        if (result == null) {
            log.warn("No job info returned from Mehnat API for userId={}, pinfl={}", user.getId(), user.getPinfl());
            return;
        }

        boolean changed = userEnrichmentService.enrichUserFromMehnat(user, result);

        if (changed) {
            user.setMehnatDataFetched(true);
            userRepository.save(user);
            log.info("User data enriched and saved for userId={}", user.getId());
        } else {
            log.info("No enrichment needed — user already up-to-date. userId={}", user.getId());
        }
    }
}
