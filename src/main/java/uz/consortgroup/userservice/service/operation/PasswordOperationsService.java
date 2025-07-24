package uz.consortgroup.userservice.service.operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.entity.Password;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.repository.PasswordRepository;
import uz.consortgroup.userservice.util.JwtUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordOperationsService {
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public String encodePassword(String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);
        log.debug("Password encoded successfully.");
        return encoded;
    }

    public Password createPassword(User user, String encodedPassword) {
        Password password = Password.builder()
                .user(user)
                .passwordHash(encodedPassword)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        log.info("Created password entity for userId={}", user.getId());
        return password;
    }

    public String generatePasswordResetToken(String email) {
        String token = jwtUtils.generatePasswordResetToken(email);
        log.info("Generated password reset token for email={}", email);
        return token;
    }

    public boolean validatePasswordResetToken(String token) {
        boolean isValid = jwtUtils.validateJwtToken(token);
        log.debug("Password reset token validation result: {}", isValid);
        return isValid;
    }

    public String extractUserIdFromToken(String token) {
        String email = jwtUtils.getUserNameFromJwtToken(token);
        log.debug("Extracted email from token: {}", email);
        return email;
    }
}
