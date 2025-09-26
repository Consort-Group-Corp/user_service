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
    public void requestPasswordReset() {
        UUID userId = authContext.getCurrentUserId();

        log.info("Requesting password reset for user ID: {}", userId);
        User user = userOperationsService.findUserById(userId);
        String userEmail = user.getEmail();
        String token = passwordOperationsService.generatePasswordResetToken(userEmail);
        passwordEventService.sendPasswordEvent(userEmail, userId, token, user.getLanguage());
        log.debug("Password reset event sent for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void updatePassword(UpdatePasswordRequestDto request, String token) {
        UUID userId = authContext.getCurrentUserId();

        log.info("Updating password for user ID: {}", userId);
        try {
            passwordValidator.validatePasswordAndToken(request, token);
            String tokenSubject = passwordOperationsService.extractUserIdFromToken(token);
            User user = userOperationsService.findUserById(userId);
            passwordValidator.validateTokenUserMatch(userId, tokenSubject, user);

            Password password = passwordRepository.findByUser(user)
                    .orElseThrow(() -> {
                        log.warn("Password record not found for user ID: {}", userId);
                        return new PasswordMismatchException("Password record not found");
                    });

            password.setPasswordHash(passwordOperationsService.encodePassword(request.getNewPassword()));
            password.setUpdatedAt(LocalDateTime.now());
            passwordRepository.save(password);

            log.debug("Password updated successfully for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Error occurred while updating password for user ID: {}", userId, e);
            throw e;
        }
    }
}
