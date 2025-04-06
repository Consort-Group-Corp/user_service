package uz.consortgroup.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.consortgroup.user_service.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.user_service.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.user_service.entity.Password;
import uz.consortgroup.user_service.entity.User;
import uz.consortgroup.user_service.repository.PasswordRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordRepository passwordRepository;
    private final PasswordEncoder passwordEncoder;


    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    public void savePassword(User user, String userPassword) {
        String encodedPassword = passwordEncoder.encode(userPassword);

        Password password = Password.builder()
                .user(user)
                .passwordHash(encodedPassword)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        passwordRepository.save(password);
    }
}
