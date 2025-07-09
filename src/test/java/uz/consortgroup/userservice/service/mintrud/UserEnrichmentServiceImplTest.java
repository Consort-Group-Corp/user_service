package uz.consortgroup.userservice.service.mintrud;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.mintrud.JobPositionResult;
import uz.consortgroup.core.api.v1.dto.mintrud.PositionDto;
import uz.consortgroup.userservice.entity.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UserEnrichmentServiceImplTest {

    private final UserEnrichmentServiceImpl userEnrichmentService = new UserEnrichmentServiceImpl();

    @Test
    void enrichUserFromMehnat_shouldEnrichAllFieldsWhenEmpty() {
        User user = new User();
        JobPositionResult result = JobPositionResult.builder()
                .name("John")
                .surname("Doe")
                .patronym("Smith")
                .positions(List.of(
                        PositionDto.builder()
                                .position("Developer")
                                .org("IT Company")
                                .tin("123456789")
                                .department("Engineering")
                                .startDate(LocalDate.of(2020, 1, 1))
                                .build()
                ))
                .build();

        boolean changed = userEnrichmentService.enrichUserFromMehnat(user, result);

        assertTrue(changed);
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("Smith", user.getMiddleName());
        assertEquals("IT Company", user.getWorkPlace());
        assertEquals("Developer", user.getPosition());
        assertEquals("123456789", user.getMehnatOrganizationTin());
        assertEquals("Engineering", user.getMehnatDepartmentName());
        assertEquals(LocalDate.of(2020, 1, 1), user.getMehnatPositionStartDate());
    }

    @Test
    void enrichUserFromMehnat_shouldNotOverrideExistingFields() {
        User user = new User();
        user.setFirstName("Existing");
        user.setPosition("Existing Position");

        JobPositionResult result = JobPositionResult.builder()
                .name("John")
                .surname("Doe")
                .patronym("Smith")
                .positions(List.of(
                        PositionDto.builder()
                                .position("Developer")
                                .org("IT Company")
                                .build()
                ))
                .build();

        boolean changed = userEnrichmentService.enrichUserFromMehnat(user, result);

        assertTrue(changed);
        assertEquals("Existing", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("Existing Position", user.getPosition());
    }

    @Test
    void enrichUserFromMehnat_shouldHandleNullPositions() {
        User user = new User();
        JobPositionResult result = JobPositionResult.builder()
                .name("John")
                .surname("Doe")
                .patronym("Smith")
                .positions(null)
                .build();

        boolean changed = userEnrichmentService.enrichUserFromMehnat(user, result);

        assertTrue(changed);
        assertEquals("John", user.getFirstName());
        assertNull(user.getWorkPlace());
        assertNull(user.getPosition());
    }

    @Test
    void enrichUserFromMehnat_shouldReturnFalseWhenNoChanges() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setMiddleName("Smith");

        JobPositionResult result = JobPositionResult.builder()
                .name("John")
                .surname("Doe")
                .patronym("Smith")
                .build();

        boolean changed = userEnrichmentService.enrichUserFromMehnat(user, result);

        assertFalse(changed);
    }

    @Test
    void enrichUserFromMehnat_shouldHandleEmptyPositionList() {
        User user = new User();
        JobPositionResult result = JobPositionResult.builder()
                .name("John")
                .surname("Doe")
                .patronym("Smith")
                .positions(List.of())
                .build();

        boolean changed = userEnrichmentService.enrichUserFromMehnat(user, result);

        assertTrue(changed);
        assertEquals("John", user.getFirstName());
        assertNull(user.getWorkPlace());
    }
}