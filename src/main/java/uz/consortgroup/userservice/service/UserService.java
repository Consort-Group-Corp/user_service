package uz.consortgroup.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterReturning;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.dto.UserProfileDto;
import uz.consortgroup.userservice.dto.UserProfileResponseDto;
import uz.consortgroup.userservice.dto.UserRegistrationDto;
import uz.consortgroup.userservice.dto.UserRegistrationResponseDto;
import uz.consortgroup.userservice.dto.UserUpdateDto;
import uz.consortgroup.userservice.dto.UserUpdateResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.enumeration.UserStatus;
import uz.consortgroup.userservice.entity.enumeration.UserRole;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.validator.UserServiceValidator;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final VerificationService verificationService;
    private final UserCacheService userCacheService;
    private final UserCacheMapper userCacheMapper;
    private final UserEventService userEventService;
    private final UserServiceValidator userServiceValidator;
    private final PasswordService passwordService;

    @Transactional
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    @AspectAfterReturning
    public UserRegistrationResponseDto registerNewUser(UserRegistrationDto userRegistrationDto) {
        userServiceValidator.validateUserRegistration(userRegistrationDto);
        User user = createUser(userRegistrationDto);

        userRepository.save(user);
        passwordService.savePassword(user, userRegistrationDto);

        String verificationCode = verificationService.generateAndSaveCode(user);

        userEventService.sendUserRegisteredEvent(user, verificationCode);
        return userMapper.toUserRegistrationResponseDto(user);
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterThrowing
    @LoggingAspectAfterMethod
    public void verifyUser(UUID userId, String inputCode) {
        User user = getUserFromDbAndCache(userId);
        verificationService.verifyCode(user, inputCode);
        userRepository.updateVerificationStatus(userId, true, UserStatus.ACTIVE);
        userRepository.updateUserRole(userId, UserRole.STUDENT);

        removeUserFromCache(userId);
        cacheUser(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found")));
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterThrowing
    @LoggingAspectAfterMethod
    public void resendVerificationCode(UUID userId) {
        userServiceValidator.validateUserId(userId);
        User user = getUserFromDbAndCache(userId);

        String verificationCode = verificationService.generateAndSaveCode(user);
        userEventService.resendVerificationCodeEvent(user, verificationCode);
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterThrowing
    @AspectAfterReturning
    @LoggingAspectAfterMethod
    public UserProfileResponseDto fillUserProfile(UUID userId, UserProfileDto userProfileDto) {
        userServiceValidator.validateUserId(userId);
        User user = updateUserProfileById(userId, userProfileDto);
        userEventService.sendUserUpdateProfileEvent(userId, userProfileDto);
        return userMapper.toUserProfileResponseDto(user);
    }

    @Transactional(readOnly = true)
    @LoggingAspectBeforeMethod
    @AspectAfterReturning
    @LoggingAspectAfterMethod
    public UserProfileResponseDto getUserById(UUID userId) {
        User user = getUserFromDbAndCache(userId);
        return userMapper.toUserProfileResponseDto(user);
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterReturning
    @AspectAfterThrowing
    @LoggingAspectAfterMethod
    public UserUpdateResponseDto updateUserById(UUID userId, UserUpdateDto updateDto) {
        userServiceValidator.checkUserRole(updateDto.getRole().name());
        userServiceValidator.validateUserId(userId);
        User user = updateUser(userId, updateDto);
        return userMapper.toUserUpdateResponseDto(user);
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterThrowing
    @LoggingAspectAfterMethod
    public void deleteUserById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }

        userRepository.deleteById(id);
        removeUserFromCache(id);
    }


    private User getUserFromDbAndCache(UUID userId) {
        log.debug("Fetching user with ID {} from cache or DB", userId);

        return userCacheService.findUserById(userId)
                .map(userCacheMapper::toUserEntity)
                .orElseGet(() -> {
                    try {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> {
                                    log.error("User with ID {} not found in database", userId);
                                    return new UserNotFoundException("User not found");
                                });
                        cacheUser(user);
                        return user;
                    } catch (Exception e) {
                        log.error("Failed to fetch user with ID {}: {}", userId, e.getMessage());
                        throw e;
                    }
                });
    }

    private static User createUser(UserRegistrationDto userRegistrationDto) {
        return User.builder()
                .language(userRegistrationDto.getLanguage())
                .email(userRegistrationDto.getEmail())
                .role(UserRole.GUEST_USER)
                .status(UserStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }


    private User updateUserProfileById(UUID userId, UserProfileDto userProfileDto) {
        return userRepository.updateUserProfileById(userId,
                        userProfileDto.getLastName(),
                        userProfileDto.getFirstName(),
                        userProfileDto.getMiddleName(),
                        userProfileDto.getBornDate(),
                        userProfileDto.getPhoneNumber(),
                        userProfileDto.getWorkPlace(),
                        userProfileDto.getPosition(),
                        userProfileDto.getPinfl())
                .orElseThrow(() -> {
                    userNotFoundLog(userId);
                    return new UserNotFoundException("User not found");
                });
    }

    private User updateUser(UUID userId, UserUpdateDto updateDto) {
        return userRepository.updateUserById(userId,
                        updateDto.getLastName(),
                        updateDto.getFirstName(),
                        updateDto.getMiddleName(),
                        updateDto.getBornDate(),
                        updateDto.getPhoneNumber(),
                        updateDto.getWorkPlace(),
                        updateDto.getEmail(),
                        updateDto.getPosition(),
                        updateDto.getPinfl(),
                        updateDto.getRole().name())
                .orElseThrow(() -> {
                    userNotFoundLog(userId);
                    return new UserNotFoundException("User not found");
                });
    }

    private void cacheUser(User user) {
        log.info("Caching user with ID: {}", user.getId());
        userCacheService.cacheUser(userCacheMapper.toUserCache(user));
    }

    private void removeUserFromCache(UUID userId) {
        log.info("Removing user with ID {} from cache", userId);
        userCacheService.removeUserFromCache(userId);
    }

    private static void userNotFoundLog(UUID userId) {
        log.error("User with ID {} not found", userId);
    }
}