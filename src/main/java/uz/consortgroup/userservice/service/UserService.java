package uz.consortgroup.userservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.dto.UserRegistrationDto;
import uz.consortgroup.userservice.dto.UserResponseDto;
import uz.consortgroup.userservice.dto.UserUpdateDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.enumeration.UserStatus;
import uz.consortgroup.userservice.entity.enumeration.UsersRole;
import uz.consortgroup.userservice.event.EventType;
import uz.consortgroup.userservice.event.UserRegistrationEvent;
import uz.consortgroup.userservice.event.VerificationCodeResentEvent;
import uz.consortgroup.userservice.exception.UserAlreadyExistsException;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.kafka.UserRegisterKafkaProducer;
import uz.consortgroup.userservice.kafka.VerificationCodeProducer;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.UserRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final VerificationService verificationService;
    private final UserCacheService userCacheService;
    private final UserCacheMapper userCacheMapper;
    private final UserRegisterKafkaProducer userRegisterKafkaProducer;
    private final VerificationCodeProducer verificationCodeProducer;
    private final PasswordEncoder passwordEncoder;
    private final AtomicLong messageIdGenerator = new AtomicLong(0);

    @Transactional
    public UserResponseDto registerNewUser(UserRegistrationDto dto) {
        log.info("Registering new user with email: {}", dto.getEmail());
        validateUserRegistration(dto);

        User user = userRepository.save(buildUserFromDto(dto));
        log.info("User registered with ID: {}", user.getId());

        String verificationCode = verificationService.generateAndSaveCode(user);
        log.info("Generated verification code for user ID: {}", user.getId());

        sendRegistrationEvent(user, verificationCode);
        cacheUser(user);

        return userMapper.toUserResponseDto(user);
    }

    @Transactional
    public void verifyUser(Long userId, String inputCode) {
        log.info("Verifying user with ID: {}", userId);
        User user = getUserFromDbAndCache(userId);

        verificationService.verifyCode(user, inputCode);
        log.info("User {} verification successful", userId);

        userRepository.updateVerificationStatus(userId, true, UserStatus.ACTIVE);
        log.info("User {} status updated to ACTIVE", userId);

        removeUserFromCache(userId);
        cacheUser(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found")));
    }

    @Transactional
    public void resendVerificationCode(Long userId) {
        log.info("Resending verification code for user ID: {}", userId);
        User user = getUserFromDbAndCache(userId);

        String verificationCode = verificationService.generateAndSaveCode(user);
        log.info("Generated new verification code for user ID: {}", userId);

        sendCodeResentEvent(user, verificationCode);
    }

    @Transactional
    public UserResponseDto getUserById(Long userId) {
        log.info("Getting user with ID: {}", userId);

        User user = getUserFromDbAndCache(userId);

        log.info("User found: {}", user.getId());

        return userMapper.toUserResponseDto(user);
    }


    @Transactional
    public UserUpdateDto updateUserById(Long userId, UserUpdateDto updateDto) {
        log.info("Updating user with ID: {}", userId);

        User user = updateUser(userId, updateDto);

        log.info("User updated: {}", user.getId());

        return userMapper.toUserUpdateDto(user);
    }

    @Transactional
    public void deleteUserById(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }

        userRepository.deleteById(id);
        removeUserFromCache(id);
        log.info("User {} deleted successfully", id);
    }

    private User getUserFromDbAndCache(Long id) {
        return userCacheService.findUserById(id)
                .map(userCacheMapper::toUserEntity)
                .orElseGet(() -> {
                    User user = userRepository.findById(id)
                            .orElseThrow(() -> {
                                log.error("User with ID {} not found in database and cache", id);
                                return new UserNotFoundException("User not found");
                            });
                    cacheUser(user);
                    return user;
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


    private void validateUserRegistration(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.error("User with email {} already exists", dto.getEmail());
            throw new UserAlreadyExistsException("User with email " + dto.getEmail() + " already exists");
        }
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

    private void sendRegistrationEvent(User user, String verificationCode) {
        UserRegistrationEvent event = UserRegistrationEvent.builder()
                .messageId(messageIdGenerator.incrementAndGet())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .email(user.getEmail())
                .verificationCode(verificationCode)
                .eventType(EventType.USER_REGISTERED)
                .build();

        userRegisterKafkaProducer.sendUserRegisterEvents(List.of(event));
    }

    private void sendCodeResentEvent(User user, String verificationCode) {
        VerificationCodeResentEvent event = VerificationCodeResentEvent.builder()
                .messageId(messageIdGenerator.incrementAndGet())
                .userId(user.getId())
                .newVerificationCode(verificationCode)
                .email(user.getEmail())
                .eventType(EventType.VERIFICATION_CODE_SENT)
                .build();

        verificationCodeProducer.sendVerificationCodeResendEvents(List.of(event));
    }
}