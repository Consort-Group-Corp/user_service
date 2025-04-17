package uz.consortgroup.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.userservice.dto.UserProfileResponseDto;
import uz.consortgroup.userservice.dto.UserRegistrationResponseDto;
import uz.consortgroup.userservice.dto.UserUpdateResponseDto;
import uz.consortgroup.userservice.dto.super_admin.UserResponseDto;
import uz.consortgroup.userservice.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserProfileResponseDto toUserProfileResponseDto(User user);
    UserRegistrationResponseDto toUserRegistrationResponseDto(User user);
    UserUpdateResponseDto toUserUpdateResponseDto(User user);
    UserResponseDto toUserResponseDto(User user);
}
