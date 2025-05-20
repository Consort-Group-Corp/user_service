package uz.consortgroup.userservice.service.super_admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserStatus;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserChangeRequestDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserCreateDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserResponseDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.event.admin.SuperAdminActionType;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.SuperAdminRepository;
import uz.consortgroup.userservice.service.event.admin.SuperAdminActionLogger;
import uz.consortgroup.userservice.service.operation.UserOperationsServiceServiceImpl;
import uz.consortgroup.userservice.service.password.PasswordServiceImpl;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminServiceImpl implements SuperAdminService {
    private final UserOperationsServiceServiceImpl userOperationsServiceImpl;
    private final UserMapper userMapper;
    private final SuperAdminRepository superAdminRepository;
    private final PasswordServiceImpl passwordServiceImpl;
    private final SuperAdminActionLogger superAdminActionLogger;

    @Transactional
    @AllAspect
    public UserResponseDto findUserByEmailAndChangeUserRole(UserChangeRequestDto userChangeRequestDto) {
        User user = userOperationsServiceImpl.changeUserRoleByEmail(userChangeRequestDto.getEmail(), userChangeRequestDto.getNewRole());
        superAdminActionLogger.userRoleChangedEvent(user, getSuperAdminId(), SuperAdminActionType.USER_UPDATED);
        return userMapper.toUserResponseDto(user);
    }


    @Transactional
    @AllAspect
    public UserResponseDto createNewUserWithMentorRole(UserCreateDto userCreateDto) {
        UUID superAdminId = getSuperAdminId();

        User user = buildUserFromDto(userCreateDto);

        userOperationsServiceImpl.saveUser(user);
        passwordServiceImpl.savePassword(user, userCreateDto.getPassword());

        superAdminActionLogger.userRoleChangedEvent(user, superAdminId, SuperAdminActionType.USER_CREATED);

        return userMapper.toUserResponseDto(user);
    }



    @Transactional(readOnly = true)
    @AllAspect
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
