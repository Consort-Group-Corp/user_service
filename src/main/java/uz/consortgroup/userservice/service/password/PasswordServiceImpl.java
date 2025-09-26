package uz.consortgroup.userservice.service.password;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.request.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.entity.Password;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.PasswordMismatchException;
import uz.consortgroup.userservice.repository.PasswordRepository;
import uz.consortgroup.userservice.security.AuthContext;
import uz.consortgroup.userservice.service.event.user.PasswordEventService;
import uz.consortgroup.userservice.service.operation.PasswordOperationsService;
import uz.consortgroup.userservice.service.operation.UserOperationsService;
import uz.consortgroup.userservice.validator.PasswordValidator;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {
    private final PasswordRepository passwordRepository;
    private final PasswordEventService passwordEventService;
    private final PasswordOperationsService passwordOperationsService;
    private final UserOperationsService userOperationsService;
    private final PasswordValidator passwordValidator;
    private final AuthContext authContext;

    @Override
    @Transactional
    public void savePassword(User user, String rawPassword) {
        log.info("Saving password for user with ID: {}", user.getId());
        String encodedPassword = passwordOperationsService.encodePassword(rawPassword);
        Password password = passwordOperationsService.createPassword(user, encodedPassword);
        passwordRepository.save(password);
        log.debug("Password saved successfully for user ID: {}", user.getId());
    }

    @Override
    @Transactional
    public void requestPasswordResetForCurrentUser() {
        UUID userId = authContext.getCurrentUserId();
        log.info("Requesting password reset for user ID: {}", userId);

        User user = userOperationsService.findUserById(userId);
        String token = passwordOperationsService.generatePasswordResetToken(user.getId().toString());
        passwordEventService.sendPasswordEvent(user.getEmail(), userId, token, user.getLanguage());

        log.debug("Password reset event sent for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void updatePassword(UpdatePasswordRequestDto request, String token) {
        log.info("Updating password by reset token");

        passwordValidator.validatePasswordAndToken(request, token);

        UUID tokenUserId = passwordOperationsService.extractUserIdFromToken(token);
        User user = userOperationsService.findUserById(tokenUserId);

        Password password = passwordRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("Password record not found for user ID: {}", tokenUserId);
                    return new PasswordMismatchException("Password record not found");
                });

        password.setPasswordHash(passwordOperationsService.encodePassword(request.getNewPassword()));
        password.setUpdatedAt(LocalDateTime.now());
        passwordRepository.save(password);

        log.debug("Password updated successfully for user ID: {}", tokenUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public void requestPasswordResetByEmail(String emailRaw) {
        final String email = (emailRaw == null) ? "" : emailRaw.trim().toLowerCase();
        if (email.isBlank()) {
            log.debug("Password reset requested with invalid/empty email");
            return;
        }

        final String masked = email.replaceAll("(^.).*(@.*$)", "$1***$2");
        log.debug("Password reset requested for email={}", masked);

        userOperationsService.findByEmailIfExists(email).ifPresent(user -> {
            String token = passwordOperationsService.generatePasswordResetToken(user.getId().toString());
            passwordEventService.sendPasswordEvent(user.getEmail(), user.getId(), token, user.getLanguage());
            log.debug("Reset email enqueued for userId={}", user.getId());
        });
    }
}
