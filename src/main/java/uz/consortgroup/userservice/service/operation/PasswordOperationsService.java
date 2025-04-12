package uz.consortgroup.userservice.service.operation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.entity.Password;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.repository.PasswordRepository;
import uz.consortgroup.userservice.util.JwtUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordOperationsService {
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public Password createPassword(User user, String encodedPassword) {
        return Password.builder()
                .user(user)
                .passwordHash(encodedPassword)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    public String generatePasswordResetToken(String email) {
        return jwtUtils.generatePasswordResetToken(email);
    }

    public boolean validatePasswordResetToken(String token) {
        return jwtUtils.validateJwtToken(token);
    }

    public String extractUserIdFromToken(String token) {
        return jwtUtils.getUserNameFromJwtToken(token);
    }
}
