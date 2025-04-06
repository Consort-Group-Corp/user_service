package uz.consortgroup.user_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.user_service.dto.*;
import uz.consortgroup.user_service.entity.User;
import uz.consortgroup.user_service.entity.enumeration.UserRole;
import uz.consortgroup.user_service.entity.enumeration.UserStatus;
import uz.consortgroup.user_service.exception.*;
import uz.consortgroup.user_service.mapper.*;
import uz.consortgroup.user_service.repository.UserRepository;
import uz.consortgroup.user_service.validator.UserServiceValidator;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(UserRole.STUDENT)
                .status(UserStatus.PENDING)
                .isVerified(false);
    }

    private UserRegistrationDto.UserRegistrationDtoBuilder registrationDtoBuilder() {
        return UserRegistrationDto.builder()
                .firstName("John")
                .lastName("Doe")
                .middleName("Middle")
                .workPlace("Company")
                .email("john@example.com")
                .position("Developer")
                .pinfl("12345678901234")
                .password("Secure123!");
    }

    @Test
    void registerNewUser_ValidData_ReturnsUserResponse() {
        UserRegistrationDto dto = registrationDtoBuilder().build();
        User savedUser = userBuilder().id(1L).build();
        UserResponseDto responseDto = UserResponseDto.builder().id(1L).build();

        doNothing().when(passwordService).savePassword(any(User.class), anyString());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserResponseDto(eq(savedUser))).thenReturn(responseDto);

        UserResponseDto result = userService.registerNewUser(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(passwordService).savePassword(any(User.class), anyString());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponseDto(eq(savedUser));
    }

    @Test
    void registerNewUser_InvalidEmail_ThrowsException() {
        UserRegistrationDto dto = registrationDtoBuilder().email("invalid").build();

        doThrow(new IllegalArgumentException("Invalid email"))
                .when(userServiceValidator).validateUserRegistration(eq(dto));

        assertThatThrownBy(() -> userService.registerNewUser(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void verifyUser_ValidCode_Success() {
        User user = userBuilder().build();
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));

        userService.verifyUser(1L, "123456");

        verify(userRepository).updateVerificationStatus(eq(1L), eq(true), eq(UserStatus.ACTIVE));
    }

    @Test
    void verifyUser_InvalidCode_ThrowsException() {
        User user = userBuilder().build();
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));
        doThrow(new InvalidVerificationCodeException("Invalid"))
                .when(verificationService).verifyCode(eq(user), eq("invalid"));

        assertThatThrownBy(() -> userService.verifyUser(1L, "invalid"))
                .isInstanceOf(InvalidVerificationCodeException.class);
    }

    @Test
    void verifyUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyUser(1L, "123456"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void resendVerificationCode_ValidUser_Success() {
        User user = userBuilder().build();
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));

        userService.resendVerificationCode(1L);

        verify(verificationService).generateAndSaveCode(eq(user));
    }

    @Test
    void resendVerificationCode_UserNotFound_ThrowsException() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resendVerificationCode(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserById_ValidUser_ReturnsUser() {
        User user = userBuilder().build();
        UserResponseDto responseDto = UserResponseDto.builder().id(1L).build();
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(eq(user))).thenReturn(responseDto);

        UserResponseDto result = userService.getUserById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getUserById_UserNotFound_ThrowsException() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUserById_ValidData_ShouldUpdateUser() {
        Long userId = 1L;
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

        verify(userServiceValidator).checkUserRole(UserRole.STUDENT.name());
        verify(userServiceValidator).validateUserId(userId);
        verify(userRepository).updateUserById(
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
                eq(updateDto.getRole().name()));

    }


    @Test
    void updateUserById_UserNotFound_ThrowsException() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .firstName("NewFirstName")
                .lastName("NewLastName")
                .email("new@email.com")
                .role(UserRole.STUDENT)
                .build();

        when(userRepository.updateUserById(
                eq(1L),
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
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserById(1L, updateDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }


    @Test
    void deleteUserById_ValidUser_Success() {
        when(userRepository.existsById(eq(1L))).thenReturn(true);

        userService.deleteUserById(1L);

        verify(userRepository).deleteById(eq(1L));
    }

    @Test
    void deleteUserById_UserNotFound_ThrowsException() {
        when(userRepository.existsById(eq(1L))).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUserById(1L))
                .isInstanceOf(UserNotFoundException.class);
    }
}