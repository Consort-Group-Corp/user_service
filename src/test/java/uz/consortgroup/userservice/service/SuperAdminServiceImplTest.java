package uz.consortgroup.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserStatus;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserChangeRequestDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserCreateDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.event.admin.SuperAdminActionType;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.SuperAdminRepository;
import uz.consortgroup.userservice.service.event.admin.SuperAdminActionLogger;
import uz.consortgroup.userservice.service.operation.UserOperationsServiceServiceImpl;
import uz.consortgroup.userservice.service.password.PasswordServiceImpl;
import uz.consortgroup.userservice.service.super_admin.SuperAdminServiceImpl;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperAdminServiceImplTest {

    private static final UUID SUPER_ADMIN_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Mock
    private UserOperationsServiceServiceImpl userOperationsServiceImpl;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SuperAdminRepository superAdminRepository;

    @Mock
    private PasswordServiceImpl passwordServiceImpl;

    @Mock
    private SuperAdminActionLogger superAdminActionLogger;

    @InjectMocks
    private SuperAdminServiceImpl superAdminServiceImpl;

    @Test
    void findUserByEmailAndChangeUserRole_ShouldReturnDtoAndLogEvent() {
        UserChangeRequestDto dto = new UserChangeRequestDto();
        dto.setEmail("user@example.com");
        dto.setNewRole(UserRole.MENTOR);

        User user = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.MENTOR)
                .build();
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(user.getId());

        when(superAdminRepository.findIdsByRole(UserRole.SUPER_ADMIN))
                .thenReturn(Stream.of(SUPER_ADMIN_ID));
        when(userOperationsServiceImpl.changeUserRoleByEmail(dto.getEmail(), dto.getNewRole()))
                .thenReturn(user);
        when(userMapper.toUserResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result = superAdminServiceImpl.findUserByEmailAndChangeUserRole(dto);

        assertEquals(responseDto, result);
        verify(userOperationsServiceImpl).changeUserRoleByEmail(dto.getEmail(), dto.getNewRole());
        verify(superAdminActionLogger).userRoleChangedEvent(user, SUPER_ADMIN_ID, SuperAdminActionType.USER_UPDATED);
    }

    @Test
    void createNewUserWithMentorRole_ShouldSaveUserPasswordAndLogEvent() {
        UserCreateDto dto = new UserCreateDto();
        dto.setLanguage(null);
        dto.setLastName("Doe");
        dto.setFirstName("John");
        dto.setMiddleName(null);
        dto.setBornDate(LocalDate.of(1990, 1, 1));
        dto.setPhoneNumber("+111111");
        dto.setWorkPlace("Company");
        dto.setEmail("john.doe@example.com");
        dto.setPosition("Developer");
        dto.setPinfl("1234");
        dto.setRole(UserRole.MENTOR);
        dto.setPassword("password");

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(user.getId());

        when(superAdminRepository.findIdsByRole(UserRole.SUPER_ADMIN))
                .thenReturn(Stream.of(SUPER_ADMIN_ID));

        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(user.getId()); // simulate ID assignment
            return null;
        }).when(userOperationsServiceImpl).saveUser(any(User.class));
        when(userMapper.toUserResponseDto(any(User.class)))
                .thenReturn(responseDto);


        UserResponseDto result = superAdminServiceImpl.createNewUserWithMentorRole(dto);


        assertEquals(responseDto, result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userOperationsServiceImpl).saveUser(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(dto.getEmail(), savedUser.getEmail());
        assertEquals(dto.getRole(), savedUser.getRole());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
        assertTrue(savedUser.getIsVerified());

        verify(passwordServiceImpl).savePassword(savedUser, dto.getPassword());
        verify(superAdminActionLogger).userRoleChangedEvent(savedUser, SUPER_ADMIN_ID, SuperAdminActionType.USER_CREATED);
    }

    @Test
    void createNewUserWithMentorRole_NoSuperAdmin_ShouldThrow() {
        UserCreateDto dto = new UserCreateDto();
        dto.setPassword("password");

        when(superAdminRepository.findIdsByRole(UserRole.SUPER_ADMIN))
                .thenReturn(Stream.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                superAdminServiceImpl.createNewUserWithMentorRole(dto)
        );
        assertEquals("Super admin not found", ex.getMessage());
        verify(userOperationsServiceImpl, never()).saveUser(any());
        verify(passwordServiceImpl, never()).savePassword(any(), any());
        verify(superAdminActionLogger, never()).userRoleChangedEvent(any(), any(), any());
    }
}
