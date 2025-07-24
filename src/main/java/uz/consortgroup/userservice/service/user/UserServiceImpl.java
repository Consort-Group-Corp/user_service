package uz.consortgroup.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserStatus;
import uz.consortgroup.core.api.v1.dto.user.request.UserProfileRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserRegistrationRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserProfileResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserRegistrationResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserUpdateResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.cache.UserCacheServiceImpl;
import uz.consortgroup.userservice.service.event.user.UserEventService;
import uz.consortgroup.userservice.service.mintrud.MehnatAutoFillService;
import uz.consortgroup.userservice.service.operation.UserOperationsServiceServiceImpl;
import uz.consortgroup.userservice.service.password.PasswordService;
import uz.consortgroup.userservice.service.verification.VerificationServiceImpl;
import uz.consortgroup.userservice.validator.UserServiceValidator;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final VerificationServiceImpl verificationServiceImpl;
    private final UserCacheServiceImpl userCacheService;
    private final UserEventService userEventService;
    private final UserServiceValidator userServiceValidator;
    private final PasswordService passwordService;
    private final UserOperationsServiceServiceImpl userOperationService;
    private final MehnatAutoFillService mehnatAutoFillService;

    @Transactional
    public UserRegistrationResponseDto registerNewUser(UserRegistrationRequestDto userRegistrationRequestDto) {
        log.info("Registering new user: {}", userRegistrationRequestDto.getEmail());
        userServiceValidator.validateUserRegistration(userRegistrationRequestDto);
        User user = createUser(userRegistrationRequestDto);

        userRepository.save(user);
        passwordService.savePassword(user, userRegistrationRequestDto.getPassword());

        String verificationCode = verificationServiceImpl.generateAndSaveCode(user);
        userEventService.sendUserRegisteredEvent(user, verificationCode);

        log.info("User {} successfully registered", user.getId());
        return userMapper.toUserRegistrationResponseDto(user);
    }

    @Transactional
    public void verifyUser(UUID userId, String inputCode) {
        log.info("Verifying user with ID {}", userId);
        User user = userOperationService.getUserFromDbAndCacheById(userId);
        verificationServiceImpl.verifyCode(user, inputCode);
        userRepository.updateVerificationStatus(userId, true, UserStatus.ACTIVE);
        userRepository.updateUserRole(userId, UserRole.STUDENT);

        removeUserFromCache(userId);
        userOperationService.cacheUser(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found")));
        log.info("User {} successfully verified", userId);
    }

    @Transactional
    public void resendVerificationCode(UUID userId) {
        log.info("Resending verification code for user {}", userId);
        userServiceValidator.validateUserId(userId);
        User user = userOperationService.getUserFromDbAndCacheById(userId);

        String verificationCode = verificationServiceImpl.generateAndSaveCode(user);
        userEventService.resendVerificationCodeEvent(user, verificationCode);
        log.info("Verification code resent for user {}", userId);
    }

    @Transactional
    public UserProfileResponseDto fillUserProfile(UUID userId, UserProfileRequestDto userProfileRequestDto) {
        log.info("Filling user profile for user {}", userId);
        userServiceValidator.validateUserId(userId);
        User user = updateUserProfileById(userId, userProfileRequestDto);
        mehnatAutoFillService.tryFetchDataFromMehnat(user);
        userEventService.sendUserUpdateProfileEvent(userId, userProfileRequestDto);
        return userMapper.toUserProfileResponseDto(user);
    }

    public UserProfileResponseDto getUserById(UUID userId) {
        log.info("Fetching profile for user {}", userId);
        User user = userOperationService.getUserFromDbAndCacheById(userId);
        return userMapper.toUserProfileResponseDto(user);
    }

    @Transactional
    public UserUpdateResponseDto updateUserById(UUID userId, UserUpdateRequestDto updateDto) {
        log.info("Updating user {}: {}", userId, updateDto.getEmail());
        userServiceValidator.checkUserRole(updateDto.getRole().name());
        userServiceValidator.validateUserId(userId);
        User user = updateUser(userId, updateDto);
        return userMapper.toUserUpdateResponseDto(user);
    }

    @Transactional
    public void deleteUserById(UUID id) {
        log.info("Deleting user with ID {}", id);
        if (!userRepository.existsById(id)) {
            log.error("Attempted to delete non-existent user {}", id);
            throw new UserNotFoundException("User not found");
        }

        userRepository.deleteById(id);
        removeUserFromCache(id);
        log.info("User {} deleted successfully", id);
    }

    private static User createUser(UserRegistrationRequestDto dto) {
        return User.builder()
                .language(dto.getLanguage())
                .email(dto.getEmail())
                .role(UserRole.GUEST_USER)
                .status(UserStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private User updateUserProfileById(UUID userId, UserProfileRequestDto dto) {
        return userRepository.updateUserProfileById(
                        userId,
                        dto.getLastName(),
                        dto.getFirstName(),
                        dto.getMiddleName(),
                        dto.getBornDate(),
                        dto.getPhoneNumber(),
                        dto.getWorkPlace(),
                        dto.getPosition(),
                        dto.getPinfl())
                .orElseThrow(() -> {
                    userNotFoundLog(userId);
                    return new UserNotFoundException("User not found");
                });
    }

    private User updateUser(UUID userId, UserUpdateRequestDto dto) {
        return userRepository.updateUserById(
                        userId,
                        dto.getLastName(),
                        dto.getFirstName(),
                        dto.getMiddleName(),
                        dto.getBornDate(),
                        dto.getPhoneNumber(),
                        dto.getWorkPlace(),
                        dto.getEmail(),
                        dto.getPosition(),
                        dto.getPinfl(),
                        dto.getRole().name())
                .orElseThrow(() -> {
                    userNotFoundLog(userId);
                    return new UserNotFoundException("User not found");
                });
    }

    private void removeUserFromCache(UUID userId) {
        log.info("Removing user {} from cache", userId);
        userCacheService.removeUserFromCache(userId);
    }

    private static void userNotFoundLog(UUID userId) {
        log.error("User {} not found", userId);
    }
}
