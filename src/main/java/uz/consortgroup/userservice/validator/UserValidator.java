package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.consortgroup.userservice.exception.DuplicateFieldException;
import uz.consortgroup.userservice.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserValidator {

    private final UserRepository userRepository;

    public void validateUniqueFields(String email, String pinfl, String phoneNumber) {
        if (StringUtils.hasText(email) && userRepository.existsByEmail(email)) {
            log.error("User with email {} already exists", email);
            throw new DuplicateFieldException("email", email, "User with this email already exists");
        }

        if (StringUtils.hasText(pinfl) && userRepository.existsByPinfl(pinfl)) {
            log.error("User with PINFL {} already exists", pinfl);
            throw new DuplicateFieldException("pinfl", pinfl, "User with this PINFL already exists");
        }

        if (StringUtils.hasText(phoneNumber) && userRepository.existsByPhoneNumber(phoneNumber)) {
            log.error("User with phone number {} already exists", phoneNumber);
            throw new DuplicateFieldException("phoneNumber", phoneNumber, "User with this phone number already exists");
        }
    }
}
