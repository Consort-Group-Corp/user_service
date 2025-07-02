package uz.consortgroup.userservice.service.mintrud;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.mintrud.JobPositionResult;
import uz.consortgroup.core.api.v1.dto.mintrud.PositionDto;
import uz.consortgroup.userservice.entity.User;

import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Service
public class UserEnrichmentServiceImpl implements UserEnrichmentService {


    @Override
    public boolean enrichUserFromMehnat(User user, JobPositionResult result) {
        boolean changed = false;

        changed |= setIfNull(user::getFirstName, user::setFirstName, result.getName());
        changed |= setIfNull(user::getLastName, user::setLastName, result.getSurname());
        changed |= setIfNull(user::getMiddleName, user::setMiddleName, result.getPatronym());

        if (result.getPositions() != null && !result.getPositions().isEmpty()) {
            PositionDto position = result.getPositions().getFirst();

            changed |= setIfNull(user::getWorkPlace, user::setWorkPlace, position.getOrg());
            changed |= setIfNull(user::getPosition, user::setPosition, position.getPosition());
        }

        return changed;
    }

    private <T> boolean setIfNull(Supplier<T> getter, Consumer<T> setter, T newValue) {
        if (getter.get() == null && newValue != null) {
            setter.accept(newValue);
            return true;
        }

        return false;
    }
}
