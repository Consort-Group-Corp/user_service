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
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserUpdateResponseDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterReturning;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.mintrud.MehnatAutoFillService;
import uz.consortgroup.userservice.service.operation.UserOperationsService;
import uz.consortgroup.userservice.service.verification.VerificationServiceImpl;
import uz.consortgroup.userservice.service.cache.UserCacheServiceImpl;
import uz.consortgroup.userservice.service.event.user.UserEventService;
import uz.consortgroup.userservice.service.operation.UserOperationsServiceServiceImpl;
import uz.consortgroup.userservice.service.password.PasswordService;
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
    @AllAspect
    public UserRegistrationResponseDto registerNewUser(UserRegistrationRequestDto userRegistrationRequestDto) {
        userServiceValidator.validateUserRegistration(userRegistrationRequestDto);
        User user = createUser(userRegistrationRequestDto);

        userRepository.save(user);
        passwordService.savePassword(user, userRegistrationRequestDto.getPassword());

        String verificationCode = verificationServiceImpl.generateAndSaveCode(user);

        userEventService.sendUserRegisteredEvent(user, verificationCode);
        return userMapper.toUserRegistrationResponseDto(user);
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterThrowing
    @LoggingAspectAfterMethod
    public void verifyUser(UUID userId, String inputCode) {
        User user = userOperationService.getUserFromDbAndCacheById(userId);
        verificationServiceImpl.verifyCode(user, inputCode);
        userRepository.updateVerificationStatus(userId, true, UserStatus.ACTIVE);
        userRepository.updateUserRole(userId, UserRole.STUDENT);

        removeUserFromCache(userId);
        userOperationService.cacheUser(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found")));
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterThrowing
    @LoggingAspectAfterMethod
    public void resendVerificationCode(UUID userId) {
        userServiceValidator.validateUserId(userId);
        User user = userOperationService.getUserFromDbAndCacheById(userId);

        String verificationCode = verificationServiceImpl.generateAndSaveCode(user);
        userEventService.resendVerificationCodeEvent(user, verificationCode);
    }

    @Transactional
    @AllAspect
    public UserProfileResponseDto fillUserProfile(UUID userId, UserProfileRequestDto userProfileRequestDto) {
        userServiceValidator.validateUserId(userId);
        User user = updateUserProfileById(userId, userProfileRequestDto);
        mehnatAutoFillService.tryFetchDataFromMehnat(user);
        userEventService.sendUserUpdateProfileEvent(userId, userProfileRequestDto);
        return userMapper.toUserProfileResponseDto(user);
    }


    @LoggingAspectBeforeMethod
    @AspectAfterReturning
    @LoggingAspectAfterMethod
    public UserProfileResponseDto getUserById(UUID userId) {
        User user = userOperationService.getUserFromDbAndCacheById(userId);
        return userMapper.toUserProfileResponseDto(user);
    }

    @Transactional
    @AllAspect
    public UserUpdateResponseDto updateUserById(UUID userId, UserUpdateRequestDto updateDto) {
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

    @Override
    @AllAspect
    public UserShortInfoResponseDto getUserShortInfoById(UUID userId) {
        User userShortInfo = userOperationService.getUserFromDbAndCacheById(userId);
        return userMapper.toUserShortInfoResponseDto(userShortInfo);
    }

    private static User createUser(UserRegistrationRequestDto userRegistrationRequestDto) {
        return User.builder()
                .language(userRegistrationRequestDto.getLanguage())
                .email(userRegistrationRequestDto.getEmail())
                .role(UserRole.GUEST_USER)
                .status(UserStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }


    private User updateUserProfileById(UUID userId, UserProfileRequestDto userProfileRequestDto) {
        return userRepository.updateUserProfileById(userId,
                        userProfileRequestDto.getLastName(),
                        userProfileRequestDto.getFirstName(),
                        userProfileRequestDto.getMiddleName(),
                        userProfileRequestDto.getBornDate(),
                        userProfileRequestDto.getPhoneNumber(),
                        userProfileRequestDto.getWorkPlace(),
                        userProfileRequestDto.getPosition(),
                        userProfileRequestDto.getPinfl())
                .orElseThrow(() -> {
                    userNotFoundLog(userId);
                    return new UserNotFoundException("User not found");
                });
    }

    private User updateUser(UUID userId, UserUpdateRequestDto updateDto) {
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

    private void removeUserFromCache(UUID userId) {
        log.info("Removing user with ID {} from cache", userId);
        userCacheService.removeUserFromCache(userId);
    }

    private static void userNotFoundLog(UUID userId) {
        log.error("User with ID {} not found", userId);
    }
}