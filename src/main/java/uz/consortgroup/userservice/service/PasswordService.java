package uz.consortgroup.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.dto.UserRegistrationDto;
import uz.consortgroup.userservice.entity.Password;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.repository.PasswordRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordRepository passwordRepository;
    private final PasswordEncoder passwordEncoder;

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    public void savePassword(User user, UserRegistrationDto userRegistrationDto) {
        String encodedPassword = passwordEncoder.encode(userRegistrationDto.getPassword());

        Password password = Password.builder()
                .user(user)
                .passwordHash(encodedPassword)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        passwordRepository.save(password);
    }
}
