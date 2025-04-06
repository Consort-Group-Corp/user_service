package uz.consortgroup.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.user_service.dto.UserResponseDto;
import uz.consortgroup.user_service.dto.UserUpdateResponseDto;
import uz.consortgroup.user_service.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);

    UserUpdateResponseDto toUserUpdateResponseDto(User user);
}
