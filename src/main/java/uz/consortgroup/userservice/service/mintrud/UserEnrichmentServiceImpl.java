package uz.consortgroup.userservice.service.mintrud;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.mintrud.JobPositionResult;
import uz.consortgroup.core.api.v1.dto.mintrud.PositionDto;
import uz.consortgroup.userservice.entity.User;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserEnrichmentServiceImpl implements UserEnrichmentService {

    @Override
    public boolean enrichUserFromMehnat(User user, JobPositionResult result) {
        boolean changed = false;

        log.info("Enriching user (userId={}, pinfl={}) with Mehnat data...", user.getId(), user.getPinfl());

        changed |= setIfNull(user::getFirstName, user::setFirstName, result.getName(), "firstName");
        changed |= setIfNull(user::getLastName, user::setLastName, result.getSurname(), "lastName");
        changed |= setIfNull(user::getMiddleName, user::setMiddleName, result.getPatronym(), "middleName");

        if (result.getPositions() != null && !result.getPositions().isEmpty()) {
            PositionDto position = result.getPositions().getFirst();

            changed |= setIfNull(user::getWorkPlace, user::setWorkPlace, position.getOrg(), "workPlace");
            changed |= setIfNull(user::getPosition, user::setPosition, position.getPosition(), "position");
            changed |= setIfNull(user::getMehnatPositionStartDate, user::setMehnatPositionStartDate, position.getStartDate(), "mehnatPositionStartDate");
            changed |= setIfNull(user::getMehnatOrganizationTin, user::setMehnatOrganizationTin, position.getTin(), "mehnatOrganizationTin");
            changed |= setIfNull(user::getMehnatDepartmentName, user::setMehnatDepartmentName, position.getDepartment(), "mehnatDepartmentName");
        }

        if (changed) {
            log.info("User (userId={}) enriched with Mehnat data successfully.", user.getId());
        } else {
            log.info("No Mehnat data changes applied to user (userId={}).", user.getId());
        }

        return changed;
    }

    private <T> boolean setIfNull(Supplier<T> getter, Consumer<T> setter, T newValue, String fieldName) {
        if (getter.get() == null && newValue != null) {
            setter.accept(newValue);
            log.debug("Field '{}' was null, updated with value: {}", fieldName, newValue);
            return true;
        }
        return false;
    }
}
