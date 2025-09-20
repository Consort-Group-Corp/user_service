package uz.consortgroup.userservice.service.super_admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import uz.consortgroup.userservice.service.operation.UserOperationsService;
import uz.consortgroup.userservice.service.password.PasswordServiceImpl;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminServiceImpl implements SuperAdminService {
    private final UserOperationsService userOperationsService;
    private final UserMapper userMapper;
    private final SuperAdminRepository superAdminRepository;
    private final PasswordServiceImpl passwordServiceImpl;
    private final SuperAdminActionLogger superAdminActionLogger;

    @Transactional
    public UserResponseDto findUserByEmailAndChangeUserRole(UserChangeRequestDto userChangeRequestDto) {
        log.info("Changing user role by email: {}, new role: {}", userChangeRequestDto.getEmail(), userChangeRequestDto.getNewRole());

        User user = userOperationsService.changeUserRoleByEmail(userChangeRequestDto.getEmail(), userChangeRequestDto.getNewRole());
        superAdminActionLogger.userRoleChangedEvent(user, getSuperAdminId(), SuperAdminActionType.USER_UPDATED);

        log.debug("User role successfully changed: userId={}, newRole={}", user.getId(), user.getRole());
        return userMapper.toUserResponseDto(user);
    }

    @Transactional
    public UserResponseDto createNewUser(UserCreateDto userCreateDto) {
        log.info("Creating a new user with role: {}", userCreateDto.getRole());

        UUID superAdminId = getSuperAdminId();
        User user = buildUserFromDto(userCreateDto);

        userOperationsService.saveUser(user);
        passwordServiceImpl.savePassword(user, userCreateDto.getPassword());

        superAdminActionLogger.userRoleChangedEvent(user, superAdminId, SuperAdminActionType.USER_CREATED);

        log.debug("New user created: id={}, email={}", user.getId(), user.getEmail());
        return userMapper.toUserResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UUID getSuperAdminId() {
        UUID id = superAdminRepository.findIdsByRole(UserRole.SUPER_ADMIN)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Super admin not found in the database");
                    return new RuntimeException("Super admin not found");
                });
        log.debug("Super admin ID retrieved: {}", id);
        return id;
    }

    private User buildUserFromDto(UserCreateDto userCreateDto) {
        log.debug("Building User entity from DTO: {}", userCreateDto.getEmail());
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
