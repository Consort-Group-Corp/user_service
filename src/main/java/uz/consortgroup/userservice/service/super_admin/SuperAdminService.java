package uz.consortgroup.userservice.service.super_admin;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserChangeRequestDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserCreateDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserResponseDto;

import java.util.UUID;

public interface SuperAdminService {
    UserResponseDto findUserByEmailAndChangeUserRole(UserChangeRequestDto userChangeRequestDto);
    UserResponseDto createNewUser(UserCreateDto userCreateDto);
    UUID getSuperAdminId();
}
