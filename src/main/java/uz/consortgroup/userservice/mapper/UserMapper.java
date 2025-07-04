package uz.consortgroup.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.core.api.v1.dto.user.response.UserProfileResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserRegistrationResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserUpdateResponseDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserResponseDto;
import uz.consortgroup.userservice.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserProfileResponseDto toUserProfileResponseDto(User user);
    UserRegistrationResponseDto toUserRegistrationResponseDto(User user);
    UserUpdateResponseDto toUserUpdateResponseDto(User user);
    UserResponseDto toUserResponseDto(User user);
    UserShortInfoResponseDto toUserShortInfoResponseDto(User user);
}
