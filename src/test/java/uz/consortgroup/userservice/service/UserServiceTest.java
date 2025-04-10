package uz.consortgroup.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.dto.*;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.enumeration.Language;
import uz.consortgroup.userservice.entity.enumeration.UserRole;
import uz.consortgroup.userservice.entity.enumeration.UserStatus;
import uz.consortgroup.userservice.exception.*;
import uz.consortgroup.userservice.mapper.*;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.validator.UserServiceValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private VerificationService verificationService;

    @Mock
    private UserCacheService userCacheService;

    @Mock
    private UserCacheMapper userCacheMapper;

    @Mock
    private UserEventService userEventService;

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserServiceValidator userServiceValidator;

    @InjectMocks
    private UserService userService;

    private User.UserBuilder userBuilder() {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(UserRole.GUEST_USER)
                .status(UserStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .isVerified(false);
    }

    private UserRegistrationDto.UserRegistrationDtoBuilder registrationDtoBuilder() {
        return UserRegistrationDto.builder()
                .email("john@example.com")
                .language(Language.ENGLISH)
                .password("Secure123!");
    }

    @Test
    void registerNewUser_ValidData_ReturnsUserRegistrationResponse() {
        UserRegistrationDto dto = registrationDtoBuilder().build();
        User savedUser = userBuilder().build();
        UserRegistrationResponseDto responseDto = UserRegistrationResponseDto.builder()
                .id(savedUser.getId())
                .build();

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            return User.builder()
                    .id(savedUser.getId())
                    .email(userToSave.getEmail())
                    .role(userToSave.getRole())
                    .status(userToSave.getStatus())
                    .build();
        });

        when(userMapper.toUserRegistrationResponseDto(any(User.class))).thenReturn(responseDto);

        doNothing().when(passwordService).savePassword(any(User.class), any(UserRegistrationDto.class));

        UserRegistrationResponseDto result = userService.registerNewUser(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedUser.getId());

        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserRegistrationResponseDto(any(User.class));
        verify(passwordService).savePassword(any(User.class), any(UserRegistrationDto.class));
    }

    @Test
    void verifyUser_ValidCode_Success() {
        UUID userId = UUID.randomUUID();
        User user = userBuilder().id(userId).build();

        when(userCacheService.findUserById(eq(userId))).thenReturn(Optional.empty());
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));

        userService.verifyUser(userId, "123456");

        verify(userRepository).updateVerificationStatus(eq(userId), eq(true), eq(UserStatus.ACTIVE));
        verify(userRepository).updateUserRole(eq(userId), eq(UserRole.STUDENT));
        verify(userCacheService).removeUserFromCache(eq(userId));
    }

    @Test
    void resendVerificationCode_ValidUser_Success() {
        UUID userId = UUID.randomUUID();
        User user = userBuilder().id(userId).build();

        when(userCacheService.findUserById(eq(userId))).thenReturn(Optional.empty());
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));

        userService.resendVerificationCode(userId);

        verify(verificationService).generateAndSaveCode(eq(user));
    }

    @Test
    void getUserById_ValidUser_ReturnsUserProfileResponse() {
        UUID userId = UUID.randomUUID();
        User user = userBuilder().id(userId).build();
        UserProfileResponseDto responseDto = UserProfileResponseDto.builder().id(userId).build();

        when(userCacheService.findUserById(eq(userId))).thenReturn(Optional.empty());
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        when(userMapper.toUserProfileResponseDto(eq(user))).thenReturn(responseDto);

        UserProfileResponseDto result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void updateUserById_ValidData_ShouldUpdateUser() {
        UUID userId = UUID.randomUUID();
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .firstName("UpdatedFirstName")
                .lastName("UpdatedLastName")
                .middleName("UpdatedMiddleName")
                .bornDate(LocalDate.of(1995, 5, 15))
                .phoneNumber("+998123456789")
                .workPlace("UpdatedWorkPlace")
                .email("updated@email.com")
                .position("UpdatedPosition")
                .pinfl("12345678901234")
                .role(UserRole.STUDENT)
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .firstName("UpdatedFirstName")
                .lastName("UpdatedLastName")
                .middleName("UpdatedMiddleName")
                .bornDate(LocalDate.of(1995, 5, 15))
                .phoneNumber("+998123456789")
                .workPlace("UpdatedWorkPlace")
                .email("updated@email.com")
                .position("UpdatedPosition")
                .pinfl("12345678901234")
                .role(UserRole.STUDENT)
                .build();

        UserUpdateResponseDto expectedResponse = UserUpdateResponseDto.builder()
                .firstName("UpdatedFirstName")
                .lastName("UpdatedLastName")
                .middleName("UpdatedMiddleName")
                .bornDate(LocalDate.of(1995, 5, 15))
                .phoneNumber("+998123456789")
                .workPlace("UpdatedWorkPlace")
                .email("updated@email.com")
                .position("UpdatedPosition")
                .pinfl("12345678901234")
                .role(UserRole.STUDENT)
                .build();

        when(userRepository.updateUserById(
                eq(userId),
                eq(updateDto.getLastName()),
                eq(updateDto.getFirstName()),
                eq(updateDto.getMiddleName()),
                eq(updateDto.getBornDate()),
                eq(updateDto.getPhoneNumber()),
                eq(updateDto.getWorkPlace()),
                eq(updateDto.getEmail()),
                eq(updateDto.getPosition()),
                eq(updateDto.getPinfl()),
                eq(updateDto.getRole().name())))
                .thenReturn(Optional.of(updatedUser));

        when(userMapper.toUserUpdateResponseDto(updatedUser))
                .thenReturn(expectedResponse);

        UserUpdateResponseDto actualResponse = userService.updateUserById(userId, updateDto);

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void deleteUserById_ValidUser_Success() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(eq(userId))).thenReturn(true);

        userService.deleteUserById(userId);

        verify(userRepository).deleteById(eq(userId));
        verify(userCacheService).removeUserFromCache(eq(userId));
    }
}