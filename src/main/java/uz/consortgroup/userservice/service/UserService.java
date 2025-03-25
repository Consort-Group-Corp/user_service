package uz.consortgroup.userservice.service;

import jakarta.persistence.Cacheable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.dto.UserCreateDto;
import uz.consortgroup.userservice.dto.UserResponseDto;
import uz.consortgroup.userservice.dto.UserUpdateDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.UserCacheEntity;
import uz.consortgroup.userservice.entity.UserStatus;
import uz.consortgroup.userservice.entity.UsersRole;
import uz.consortgroup.userservice.exception.UserAlreadyExistsException;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.util.JwtUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final UserCacheMapper userCacheMapper;
    private final UserCacheService userCacheService;

    @Transactional
    public UserCreateDto registerNewUser(UserCreateDto userCreateDto) {
        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + userCreateDto.getEmail() + " already exists");
        }

        User user = User.builder()
                .firstName(userCreateDto.getFirstName())
                .middleName(userCreateDto.getMiddleName())
                .lastName(userCreateDto.getLastName())
                .workPlace(userCreateDto.getWorkPlace())
                .email(userCreateDto.getEmail())
                .position(userCreateDto.getPosition())
                .pinfl(userCreateDto.getPinfl())
                .usersRole(UsersRole.STUDENT)
                .userStatus(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserResponseDto findUserById(Long id) {
        UserCacheEntity userFromCache = userCacheService.findUsersById(id);

        if (userFromCache != null) {
            log.info("User found in cache: {}", id);
            return userCacheMapper.toDto(userFromCache);
        }

        User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        log.info("User found in database: {}", id);

        log.info("Saving user to cache: {}", id);
        userCacheService.saveUsersToCache(userCacheMapper.toUserEntity(user));
        return userMapper.toUserResponseDto(user);
    }

    @Transactional
    public UserUpdateDto updateUserById(Long id, UserUpdateDto userUpdateDto) {
        log.info("Starting user update with id: {}", id);

        User user = userRepository.updateUserById(id,
                userUpdateDto.getFirstName(),
                userUpdateDto.getMiddleName(),
                userUpdateDto.getLastName(),
                userUpdateDto.getWorkPlace(),
                userUpdateDto.getEmail(),
                userUpdateDto.getPosition(),
                userUpdateDto.getPinfl()
        ).orElseThrow(() -> {
            log.error("User with id {} not found", id);
            return new UserNotFoundException(String.format("User with id %d not found", id));
        });

        log.info("User with id {} successfully updated: {}", id, user);

        UserUpdateDto updatedUserDto = userMapper.toUpdateDto(user);
        log.debug("Returning updated user data: {}", updatedUserDto);

        return updatedUserDto;
    }

    @Transactional
    public void deleteUserById(Long id) {
        log.info("Deleting user with id: {}", id);
        userRepository.deleteById(id);
    }
}
