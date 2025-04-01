package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.dto.UserRegistrationDto;
import uz.consortgroup.userservice.exception.UserAlreadyExistsException;
import uz.consortgroup.userservice.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserServiceValidator {
    private final UserRepository userRepository;

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void validateUserRegistration(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + dto.getEmail() + " already exists");
        }
    }
}
