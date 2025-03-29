package uz.consortgroup.userservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.adapter.VerifiableUserAdapter;
import uz.consortgroup.userservice.dto.*;
import uz.consortgroup.userservice.entity.*;
import uz.consortgroup.userservice.event.EventType;
import uz.consortgroup.userservice.exception.*;
import uz.consortgroup.userservice.kafka.UserRegisterKafkaProducer;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.util.JwtUtils;
import uz.consortgroup.userservice.util.SecureCodeGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final UserCacheMapper userCacheMapper;
    private final UserCacheService userCacheService;
    private final SecureCodeGenerator secureCodeGenerator;
    private final UserRegisterKafkaProducer userRegisterKafkaProducer;
    private final PasswordEncoder passwordEncoder;
    private final AtomicLong messageIdGenerator = new AtomicLong(0);

    @Transactional
    public UserResponseDto registerNewUser(UserRegistrationDto userRegistrationDto) {
        log.info("Starting registration for user with email: {}", userRegistrationDto.getEmail());
        validateUserRegistration(userRegistrationDto);

        User user = buildNewUser(userRegistrationDto);
        generateVerificationCode(user);

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());

        userCacheService.saveUsersToCache(userCacheMapper.toUserEntity(user));
        log.debug("User cached successfully with ID: {}", user.getId());

        sendVerificationCodeEvent(user);
        return userMapper.toUserResponseDto(user);
    }

    @Transactional
    public void verifyUser(Long userId, String verificationCode) {
        log.info("Starting verification for user ID: {}", userId);
        User user = getUserFromCacheOrDb(userId);
        validateVerificationCode(new VerifiableUserAdapter(user), verificationCode);

        updateUserVerificationStatus(user);
        userCacheService.saveUsersToCache(userCacheMapper.toUserEntity(user));
        log.info("User verified successfully with ID: {}", userId);

    }

    @Transactional
    public void resendVerificationCode(Long userId) {
        log.info("Resending verification code for user ID: {}", userId);
        User user = getUserFromCacheOrDb(userId);
        generateVerificationCode(user);

        userRepository.save(user);
        userCacheService.saveUsersToCache(userCacheMapper.toUserEntity(user));
        log.info("New verification code generated for user ID: {}", userId);

        log.info("Sending verification code event for user ID: {}", userId);
        sendVerificationCodeEvent(user);
    }

    public UserResponseDto findUserById(Long id) {
        log.debug("Looking for user by ID: {}", id);
        return userCacheService.findUsersById(id)
                .map(userCacheMapper::toDto)
                .orElseGet(() -> getUserFromDbAndSaveToCache(id));
    }

    @Transactional
    public UserResponseDto updateUserById(Long id, UserUpdateDto userUpdateDto) {
        log.info("Updating user with ID: {}", id);
        User user = userRepository.updateUserById(id,
                userUpdateDto.getFirstName(),
                userUpdateDto.getMiddleName(),
                userUpdateDto.getLastName(),
                userUpdateDto.getWorkPlace(),
                userUpdateDto.getEmail(),
                userUpdateDto.getPosition(),
                userUpdateDto.getPinfl()
        ).orElseThrow(() -> {
            log.error("User with ID {} not found for update", id);
            return new UserNotFoundException(String.format("User with id %d not found", id));
        });

        userCacheService.saveUsersToCache(userCacheMapper.toUserEntity(user));
        log.info("User updated successfully with ID: {}", id);

        return userMapper.toUserResponseDto(user);
    }

    @Transactional
    public void deleteUserById(Long id) {
        log.info("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    private void sendVerificationCodeEvent(User user) {
        try {
            VerificationKafkaDto dto = VerificationKafkaDto.builder()
                    .userId(user.getId())
                    .firstName(user.getFirstName())
                    .middleName(user.getMiddleName())
                    .email(user.getEmail())
                    .verificationCode(user.getVerificationCode())
                    .eventType(EventType.USER_REGISTERED)
                    .messageId(messageIdGenerator.incrementAndGet())
                    .build();

            userRegisterKafkaProducer.send(List.of(dto));
            log.info("Registration event sent successfully for user ID: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to send registration event for user ID: {}", user.getId(), e);
            throw new EventPublishingException("Failed to send registration event");
        }
    }

    private UserResponseDto getUserFromDbAndSaveToCache(Long id) {
        log.debug("User not found in cache, checking database for ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found in database with ID: {}", id);
                    return new EntityNotFoundException("User not found");
                });

        userCacheService.saveUsersToCache(userCacheMapper.toUserEntity(user));
        log.debug("User loaded from DB and cached with ID: {}", id);
        return userMapper.toUserResponseDto(user);
    }

    private void validateUserRegistration(UserRegistrationDto dto) {
        log.debug("Validating user registration for email: {}", dto.getEmail());
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.error("User already exists with email: {}", dto.getEmail());
            throw new UserAlreadyExistsException("User with email " + dto.getEmail() + " already exists");
        }
    }

    private User buildNewUser(UserRegistrationDto dto) {
        log.debug("Building new user entity for email: {}", dto.getEmail());
        return User.builder()
                .lastName(dto.getLastName())
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .workPlace(dto.getWorkPlace())
                .email(dto.getEmail())
                .position(dto.getPosition())
                .pinfl(dto.getPinfl())
                .password(passwordEncoder.encode(dto.getPassword()))
                .isVerified(false)
                .usersRole(UsersRole.STUDENT)
                .userStatus(UserStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void generateVerificationCode(User user) {
        log.debug("Generating verification code for user ID: {}", user.getId());
        user.setVerificationCode(secureCodeGenerator.generateSecureCode());
        user.setVerificationCodeExpiredAt(LocalDateTime.now().plusMinutes(5));
        log.info("Verification code generated for user ID: {}", user.getId());
    }

    private void updateUserVerificationStatus(User user) {
        log.debug("Updating verification status for user ID: {}", user.getId());
        user.setIsVerified(true);
        user.setUserStatus(UserStatus.ACTIVE);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiredAt(null);
        user.setUpdatedAt(LocalDateTime.now());
        log.info("User verification status updated for ID: {}", user.getId());
    }

    private void validateVerificationCode(VerifiableUserAdapter user, String verificationCode) {
        log.debug("Validating verification code for user ID: {}", user.getId());
        if (user.getVerificationCode() == null) {
            log.error("Verification code not found for user ID: {}", user.getId());
            throw new InvalidVerificationCodeException("Verification code not found");
        }

        if (!user.getVerificationCode().equals(verificationCode)) {
            log.error("Invalid verification code for user ID: {}", user.getId());
            throw new InvalidVerificationCodeException("Invalid verification code");
        }

        if (user.getVerificationCodeExpiredAt().isBefore(LocalDateTime.now())) {
            log.error("Verification code expired for user ID: {}", user.getId());
            throw new VerificationCodeExpiredException("Verification code expired");
        }
        log.debug("Verification code validated successfully for user ID: {}", user.getId());
    }

    private User getUserFromCacheOrDb(Long userId) {
        log.debug("Getting user from cache or DB for ID: {}", userId);
        return userCacheService.findUsersById(userId)
                .map(userCacheEntity -> {
                    log.debug("User found in cache for ID: {}", userId);
                    return userCacheMapper.toUserCache(userCacheEntity);
                })
                .orElseGet(() -> {
                    log.debug("User not found in cache, checking DB for ID: {}", userId);
                    return userRepository.findById(userId)
                            .orElseThrow(() -> {
                                log.error("User not found in cache or DB for ID: {}", userId);
                                return new EntityNotFoundException("User not found");
                            });
                });
    }
}