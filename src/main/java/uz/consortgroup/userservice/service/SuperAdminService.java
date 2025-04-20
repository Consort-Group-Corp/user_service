package uz.consortgroup.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterReturning;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminService {
    private final UserOperationsService userOperationsService;
    private final UserMapper userMapper;
    private final SuperAdminRepository superAdminRepository;
    private final PasswordService passwordService;
    private final AdminActionLogger adminActionLogger;

    @Transactional
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterReturning
    @AspectAfterThrowing
    public UserResponseDto findUserByEmailAndChangeUserRole(UserChangeRequestDto userChangeRequestDto) {
        User user = userOperationsService.changeUserRoleByEmail(userChangeRequestDto.getEmail(), userChangeRequestDto.getNewRole());
        adminActionLogger.logUserCreationByAdmin(user, getSuperAdminId(), ActionType.USER_UPDATED);
        return userMapper.toUserResponseDto(user);
    }


    @Transactional
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterReturning
    @AspectAfterThrowing
    public UserResponseDto createNewUserWithMentorRole(UserCreateDto userCreateDto) {
        UUID superAdminId = getSuperAdminId();

        User user = buildUserFromDto(userCreateDto);

        userOperationsService.saveUser(user);
        passwordService.savePassword(user, userCreateDto.getPassword());

        adminActionLogger.logUserCreationByAdmin(user, superAdminId, ActionType.USER_CREATED);

        return userMapper.toUserResponseDto(user);
    }



    @Transactional(readOnly = true)
    public UUID getSuperAdminId() {
        return superAdminRepository.findIdsByRole(UserRole.SUPER_ADMIN)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Super admin not found"));
    }

    private User buildUserFromDto(UserCreateDto userCreateDto) {
        return User.builder()
                .language(userCreateDto.getLanguage())
                .lastName(userCreateDto.getLastName())
                .firstName(userCreateDto.getFirstName())
                .middleName(userCreateDto.getMiddleName())
                .bornDate(userCreateDto.getBornDate())
                .phoneNumber(userCreateDto.getPhoneNumber())
                .workPlace(userCreateDto.getWorkPlace())
                .email(userCreateDto.getEmail())
                .position(userCreateDto.getPosition())
                .pinfl(userCreateDto.getPinfl())
                .isVerified(true)
                .status(UserStatus.ACTIVE)
                .role(userCreateDto.getRole())
                .build();
    }
}
