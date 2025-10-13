package uz.consortgroup.userservice.service.operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.entity.Password;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.util.JwtUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordOperationsService {
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public String encodePassword(String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);
        log.debug("Password encoded successfully");
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

    public String generatePasswordResetToken(String userIdStr) {
        String token = jwtUtils.generatePasswordResetToken(userIdStr);
        log.debug("Generated password reset token for userId={}", userIdStr);
        return token;
    }

    public boolean validatePasswordResetToken(String token) {
        boolean isValid = jwtUtils.validateResetToken(token);
        log.debug("Password reset token validation result: {}", isValid);
        return isValid;
    }

    public UUID extractUserIdFromToken(String token) {
        UUID userId = jwtUtils.subjectAsUserId(token);
        log.debug("Extracted userId from reset token");
        return userId;
    }

    public boolean consumeResetToken(String token) {
        return jwtUtils.consumeResetToken(token);
    }
}
