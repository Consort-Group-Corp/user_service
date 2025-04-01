package uz.consortgroup.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterReturning;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.dto.UserRegistrationDto;
import uz.consortgroup.userservice.dto.UserResponseDto;
import uz.consortgroup.userservice.dto.UserUpdateDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.enumeration.UserStatus;
import uz.consortgroup.userservice.entity.enumeration.UsersRole;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.validator.UserServiceValidator;

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
    private final PasswordEncoder passwordEncoder;
    private final UserServiceValidator userServiceValidator;

    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterThrowing
    @AspectAfterReturning
    @LoggingAspectAfterMethod
    public UserResponseDto registerNewUser(UserRegistrationDto dto) {
        userServiceValidator.validateUserRegistration(dto);
        User user = userRepository.save(buildUserFromDto(dto));

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

        removeUserFromCache(userId);
        cacheUser(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found")));
    }

    @Transactional
    @LoggingAspectBeforeMethod
    @AspectAfterThrowing
    @LoggingAspectAfterMethod
    public void resendVerificationCode(Long userId) {
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
    public UserUpdateDto updateUserById(Long userId, UserUpdateDto updateDto) {
        User user = updateUser(userId, updateDto);
        return userMapper.toUserUpdateDto(user);
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
                updateDto.getFirstName(),
                updateDto.getMiddleName(),
                updateDto.getLastName(),
                updateDto.getWorkPlace(),
                updateDto.getEmail(),
                updateDto.getPinfl(),
                updateDto.getPosition()).orElseThrow(() -> {
            log.error("User with ID {} not found", userId);
            return new UserNotFoundException("User not found");});
    }


    private User buildUserFromDto(UserRegistrationDto dto) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .middleName(dto.getMiddleName())
                .workPlace(dto.getWorkPlace())
                .email(dto.getEmail())
                .position(dto.getPosition())
                .pinfl(dto.getPinfl())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UsersRole.STUDENT)
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