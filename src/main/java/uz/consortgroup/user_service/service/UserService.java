package uz.consortgroup.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.user_service.asspect.annotation.AspectAfterReturning;
import uz.consortgroup.user_service.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.user_service.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.user_service.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.user_service.dto.UserRegistrationDto;
import uz.consortgroup.user_service.dto.UserResponseDto;
import uz.consortgroup.user_service.dto.UserUpdateDto;
import uz.consortgroup.user_service.dto.UserUpdateResponseDto;
import uz.consortgroup.user_service.entity.User;
import uz.consortgroup.user_service.entity.enumeration.UserStatus;
import uz.consortgroup.user_service.entity.enumeration.UserRole;
import uz.consortgroup.user_service.exception.UserNotFoundException;
import uz.consortgroup.user_service.mapper.UserCacheMapper;
import uz.consortgroup.user_service.mapper.UserMapper;
import uz.consortgroup.user_service.repository.UserRepository;
import uz.consortgroup.user_service.validator.UserServiceValidator;

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
    @AspectAfterThrowing
    @AspectAfterReturning
    @LoggingAspectAfterMethod
    public UserResponseDto registerNewUser(UserRegistrationDto dto) {
        userServiceValidator.validateUserRegistration(dto);
        User user = userRepository.save(buildUserFromDto(dto));
        passwordService.savePassword(user, dto.getPassword());

        String verificationCode = verificationService.generateAndSaveCode(user);
        userEventService.sendRegistrationEvent(user, verificationCode);
        cacheUser(user);

        return userMapper.toUserResponseDto(user);
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterThrowing
    @LoggingAspectAfterMethod
    public void verifyUser(Long userId, String inputCode) {

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
    public void resendVerificationCode(Long userId) {
        userServiceValidator.validateUserId(userId);
        User user = getUserFromDbAndCache(userId);

        String verificationCode = verificationService.generateAndSaveCode(user);
        userEventService.resendVerificationCodeEvent(user, verificationCode);
    }

    @Transactional(readOnly = true)
    @LoggingAspectBeforeMethod
    @AspectAfterReturning
    @LoggingAspectAfterMethod
    public UserResponseDto getUserById(Long userId) {
        User user = getUserFromDbAndCache(userId);
        return userMapper.toUserResponseDto(user);
    }


    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterReturning
    @AspectAfterThrowing
    @LoggingAspectAfterMethod
    public UserUpdateResponseDto updateUserById(Long userId, UserUpdateDto updateDto) {
        userServiceValidator.checkUserRole(updateDto.getRole().name());
        userServiceValidator.validateUserId(userId);
        User user = updateUser(userId, updateDto);
        return userMapper.toUserUpdateResponseDto(user);
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterThrowing
    @LoggingAspectAfterMethod
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }

        userRepository.deleteById(id);
        removeUserFromCache(id);
    }


    private User getUserFromDbAndCache(Long userId) {
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

    private User updateUser(Long userId, UserUpdateDto updateDto) {
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
                    log.error("User with ID {} not found", userId);
                    return new UserNotFoundException("User not found");
                });
    }


    private User buildUserFromDto(UserRegistrationDto dto) {
        return User.builder()
                .language(dto.getLanguage())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .middleName(dto.getMiddleName())
                .workPlace(dto.getWorkPlace())
                .bornDate(dto.getBornDate())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .position(dto.getPosition())
                .pinfl(dto.getPinfl())
                .role(UserRole.GUEST_USER)
                .status(UserStatus.PENDING)
                .isVerified(false)
                .build();
    }

    private void cacheUser(User user) {
        log.info("Caching user with ID: {}", user.getId());
        userCacheService.cacheUser(userCacheMapper.toUserCache(user));
    }

    private void removeUserFromCache(Long userId) {
        log.info("Removing user with ID {} from cache", userId);
        userCacheService.removeUserFromCache(userId);
    }

}