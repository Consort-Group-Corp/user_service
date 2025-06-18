package uz.consortgroup.userservice.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Language;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserStatus;
import uz.consortgroup.core.api.v1.dto.user.request.UserRegistrationRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserProfileResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserRegistrationResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserUpdateResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.cache.UserCacheServiceImpl;
import uz.consortgroup.userservice.service.event.user.UserEventService;
import uz.consortgroup.userservice.service.operation.UserOperationsServiceServiceImpl;
import uz.consortgroup.userservice.service.password.PasswordServiceImpl;
import uz.consortgroup.userservice.service.verification.VerificationServiceImpl;
import uz.consortgroup.userservice.validator.UserServiceValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private VerificationServiceImpl verificationServiceImpl;

    @Mock
    private UserCacheServiceImpl userCacheService;

    @Mock
    private UserCacheMapper userCacheMapper;

    @Mock
    private UserEventService userEventService;

    @Mock
    private PasswordServiceImpl passwordServiceImpl;

    @Mock
    private UserOperationsServiceServiceImpl userOperationsServiceImpl;

    @Mock
    private UserServiceValidator userServiceValidator;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

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

    private UserRegistrationRequestDto.UserRegistrationRequestDtoBuilder registrationDtoBuilder() {
        return UserRegistrationRequestDto.builder()
                .email("john@example.com")
                .language(Language.ENGLISH)
                .password("Secure123!");
    }

    @Test
    void registerNewUser_ValidData_ReturnsUserRegistrationResponse() {
        UserRegistrationRequestDto dto = registrationDtoBuilder().build();
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

        doNothing().when(passwordServiceImpl).savePassword(any(User.class), anyString());

        UserRegistrationResponseDto result = userServiceImpl.registerNewUser(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedUser.getId());

        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserRegistrationResponseDto(any(User.class));
        verify(passwordServiceImpl).savePassword(any(User.class), anyString());
    }

    @Test
    void verifyUser_ValidCode_Success() {
        UUID userId = UUID.randomUUID();
        User user = userBuilder().id(userId).build();

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));

        userServiceImpl.verifyUser(userId, "123456");

        verify(userRepository).updateVerificationStatus(eq(userId), eq(true), eq(UserStatus.ACTIVE));
        verify(userRepository).updateUserRole(eq(userId), eq(UserRole.STUDENT));
        verify(userCacheService).removeUserFromCache(eq(userId));
    }

    @Test
    void resendVerificationCode_ValidUser_Success() {
        UUID userId = UUID.randomUUID();
        User user = userBuilder().id(userId).build();

        doNothing().when(userServiceValidator).validateUserId(eq(userId));

        when(userOperationsServiceImpl.getUserFromDbAndCacheById(eq(userId))).thenReturn(user);
        when(verificationServiceImpl.generateAndSaveCode(eq(user))).thenReturn("verificationCode");

        doNothing().when(userEventService).resendVerificationCodeEvent(eq(user), eq("verificationCode"));

        userServiceImpl.resendVerificationCode(userId);

        verify(verificationServiceImpl).generateAndSaveCode(eq(user));  // Проверяем, что метод вызвался с правильным пользователем
        verify(userEventService).resendVerificationCodeEvent(eq(user), eq("verificationCode"));
    }

    @Test
    void getUserById_ValidUser_ReturnsUserProfileResponse() {
        UUID userId = UUID.randomUUID();
        User user = userBuilder().id(userId).build();
        UserProfileResponseDto responseDto = UserProfileResponseDto.builder().id(userId).build();


        when(userOperationsServiceImpl.getUserFromDbAndCacheById(userId)).thenReturn(user);
        when(userMapper.toUserProfileResponseDto(user)).thenReturn(responseDto);

        UserProfileResponseDto result = userServiceImpl.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);

        verify(userOperationsServiceImpl).getUserFromDbAndCacheById(userId);
        verify(userMapper).toUserProfileResponseDto(user);
    }


    @Test
    void updateUserById_ValidData_ShouldUpdateUser() {
        UUID userId = UUID.randomUUID();
        UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
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

        UserUpdateResponseDto actualResponse = userServiceImpl.updateUserById(userId, updateDto);

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void deleteUserById_ValidUser_Success() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(eq(userId))).thenReturn(true);

        userServiceImpl.deleteUserById(userId);

        verify(userRepository).deleteById(eq(userId));
        verify(userCacheService).removeUserFromCache(eq(userId));
    }
}