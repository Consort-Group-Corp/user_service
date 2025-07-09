package uz.consortgroup.userservice.service.mintrud;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.mintrud.JobPositionResult;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MehnatAutoFillServiceImplTest {

    @Mock
    private MintrudIntegrationService mintrudIntegrationService;

    @Mock
    private UserEnrichmentService userEnrichmentService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MehnatAutoFillServiceImpl mehnatAutoFillService;

    @Test
    @Transactional
    void tryFetchDataFromMehnat_shouldFetchAndSaveDataWhenPinflExistsAndNotFetched() {
        User user = new User();
        user.setPinfl("12345678901234");
        user.setMehnatDataFetched(false);

        JobPositionResult result = new JobPositionResult();
        when(mintrudIntegrationService.getJobInfo(user.getPinfl())).thenReturn(result);
        when(userEnrichmentService.enrichUserFromMehnat(user, result)).thenReturn(true);

        mehnatAutoFillService.tryFetchDataFromMehnat(user);

        verify(mintrudIntegrationService).getJobInfo(user.getPinfl());
        verify(userEnrichmentService).enrichUserFromMehnat(user, result);
        verify(userRepository).save(user);
        assertTrue(user.getMehnatDataFetched());
    }

    @Test
    void tryFetchDataFromMehnat_shouldDoNothingWhenPinflIsNull() {
        User user = new User();
        user.setPinfl(null);

        mehnatAutoFillService.tryFetchDataFromMehnat(user);

        verifyNoInteractions(mintrudIntegrationService);
        verifyNoInteractions(userEnrichmentService);
        verifyNoInteractions(userRepository);
    }

    @Test
    void tryFetchDataFromMehnat_shouldDoNothingWhenDataAlreadyFetched() {

        User user = new User();
        user.setPinfl("12345678901234");
        user.setMehnatDataFetched(true);

        mehnatAutoFillService.tryFetchDataFromMehnat(user);

        verifyNoInteractions(mintrudIntegrationService);
        verifyNoInteractions(userEnrichmentService);
        verifyNoInteractions(userRepository);
    }

    @Test
    void tryFetchDataFromMehnat_shouldNotSaveWhenNoChangesMade() {
        User user = new User();
        user.setPinfl("12345678901234");
        user.setMehnatDataFetched(false);

        JobPositionResult result = new JobPositionResult();
        when(mintrudIntegrationService.getJobInfo(user.getPinfl())).thenReturn(result);
        when(userEnrichmentService.enrichUserFromMehnat(user, result)).thenReturn(false);

        mehnatAutoFillService.tryFetchDataFromMehnat(user);

        verify(mintrudIntegrationService).getJobInfo(user.getPinfl());
        verify(userEnrichmentService).enrichUserFromMehnat(user, result);
        verify(userRepository, never()).save(any());

        assertFalse(user.getMehnatDataFetched());
    }

    @Test
    void tryFetchDataFromMehnat_shouldHandleNullResultFromIntegrationService() {
        User user = new User();
        user.setPinfl("12345678901234");
        user.setMehnatDataFetched(false);

        when(mintrudIntegrationService.getJobInfo(user.getPinfl())).thenReturn(null);

        mehnatAutoFillService.tryFetchDataFromMehnat(user);

        verify(mintrudIntegrationService).getJobInfo(user.getPinfl());
        verifyNoInteractions(userEnrichmentService);
        verifyNoInteractions(userRepository);
    }
}