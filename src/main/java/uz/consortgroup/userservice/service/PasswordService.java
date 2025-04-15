package uz.consortgroup.userservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.dto.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.entity.Password;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.PasswordMismatchException;
import uz.consortgroup.userservice.repository.PasswordRepository;
import uz.consortgroup.userservice.service.event.PasswordEventService;
import uz.consortgroup.userservice.service.operation.PasswordOperations;
import uz.consortgroup.userservice.service.operation.PasswordOperationsService;
import uz.consortgroup.userservice.service.operation.UserOperations;
import uz.consortgroup.userservice.validator.PasswordValidator;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService implements PasswordOperations {
    private final PasswordRepository passwordRepository;
    private final PasswordEventService passwordEventService;
    private final PasswordOperationsService passwordOperationsService;
    private final UserOperations userOperations;
    private final PasswordValidator passwordValidator;

    @Override
    @Transactional
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    public void savePassword(User user, String rawPassword) {
        String encodedPassword = passwordOperationsService.encodePassword(rawPassword);
        Password password = passwordOperationsService.createPassword(user, encodedPassword);
        passwordRepository.save(password);
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    public void requestPasswordReset(UUID userId) {
        User user = userOperations.findUserById(userId);
        String userEmail = user.getEmail();
        String token = passwordOperationsService.generatePasswordResetToken(userEmail);
        passwordEventService.sendPasswordEvent(userEmail, userId, token);
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void updatePassword(UUID userId, UpdatePasswordRequestDto request, String token) {
        passwordValidator.validatePasswordAndToken(request, token);

        String tokenSubject = passwordOperationsService.extractUserIdFromToken(token);

        User user = userOperations.findUserById(userId);

        passwordValidator.validateTokenUserMatch(userId, tokenSubject, user);

        Password password = passwordRepository.findByUser(user)
                .orElseThrow(() -> new PasswordMismatchException("Password record not found"));

        password.setPasswordHash(passwordOperationsService.encodePassword(request.getNewPassword()));
        password.setUpdatedAt(LocalDateTime.now());
        passwordRepository.save(password);
    }
}

