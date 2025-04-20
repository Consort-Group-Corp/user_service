package uz.consortgroup.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.dto.super_admin.UserChangeRequestDto;
import uz.consortgroup.userservice.dto.super_admin.UserCreateDto;
import uz.consortgroup.userservice.dto.super_admin.UserResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.enumeration.UserRole;
import uz.consortgroup.userservice.entity.enumeration.UserStatus;
import uz.consortgroup.userservice.event.admin.ActionType;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.SuperAdminRepository;
import uz.consortgroup.userservice.service.event.admin.AdminActionLogger;
import uz.consortgroup.userservice.service.operation.UserOperationsService;

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
class SuperAdminServiceTest {

    private static final UUID SUPER_ADMIN_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Mock
    private UserOperationsService userOperationsService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SuperAdminRepository superAdminRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private AdminActionLogger adminActionLogger;

    @InjectMocks
    private SuperAdminService superAdminService;

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
        when(userOperationsService.changeUserRoleByEmail(dto.getEmail(), dto.getNewRole()))
                .thenReturn(user);
        when(userMapper.toUserResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result = superAdminService.findUserByEmailAndChangeUserRole(dto);

        assertEquals(responseDto, result);
        verify(userOperationsService).changeUserRoleByEmail(dto.getEmail(), dto.getNewRole());
        verify(adminActionLogger).logUserCreationByAdmin(user, SUPER_ADMIN_ID, ActionType.USER_UPDATED);
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
        }).when(userOperationsService).saveUser(any(User.class));
        when(userMapper.toUserResponseDto(any(User.class)))
                .thenReturn(responseDto);


        UserResponseDto result = superAdminService.createNewUserWithMentorRole(dto);


        assertEquals(responseDto, result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userOperationsService).saveUser(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(dto.getEmail(), savedUser.getEmail());
        assertEquals(dto.getRole(), savedUser.getRole());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
        assertTrue(savedUser.getIsVerified());

        verify(passwordService).savePassword(savedUser, dto.getPassword());
        verify(adminActionLogger).logUserCreationByAdmin(savedUser, SUPER_ADMIN_ID, ActionType.USER_CREATED);
    }

    @Test
    void createNewUserWithMentorRole_NoSuperAdmin_ShouldThrow() {
        UserCreateDto dto = new UserCreateDto();
        dto.setPassword("password");

        when(superAdminRepository.findIdsByRole(UserRole.SUPER_ADMIN))
                .thenReturn(Stream.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                superAdminService.createNewUserWithMentorRole(dto)
        );
        assertEquals("Super admin not found", ex.getMessage());
        verify(userOperationsService, never()).saveUser(any());
        verify(passwordService, never()).savePassword(any(), any());
        verify(adminActionLogger, never()).logUserCreationByAdmin(any(), any(), any());
    }
}
