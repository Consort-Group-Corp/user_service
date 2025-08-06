package uz.consortgroup.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.core.api.v1.dto.user.response.UserBulkSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserProfileResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserRegistrationResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserUpdateResponseDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserResponseDto;
import uz.consortgroup.userservice.entity.User;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserProfileResponseDto toUserProfileResponseDto(User user);
    UserRegistrationResponseDto toUserRegistrationResponseDto(User user);
    UserUpdateResponseDto toUserUpdateResponseDto(User user);
    UserResponseDto toUserResponseDto(User user);

    @Mapping(target = "role", source = "role")
    UserShortInfoResponseDto toUserShortInfoResponseDto(User user);
    List<UserShortInfoResponseDto> toUserShortInfoResponseDtos(List<User> users);
    @Mapping(target = "userId", source = "id")
    UserSearchResponse toUserSearchResponse(User user);


    default UserBulkSearchResponse toUserBulkSearchResponse(List<User> users) {
        List<UserSearchResponse> userDtos = users.stream()
                .map(this::toUserSearchResponse)
                .toList();
        return new UserBulkSearchResponse(userDtos);
    }
}
