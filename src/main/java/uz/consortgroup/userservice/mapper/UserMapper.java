package uz.consortgroup.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.userservice.dto.UserResponseDto;
import uz.consortgroup.userservice.dto.UserUpdateDto;
import uz.consortgroup.userservice.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);
    UserUpdateDto toUserUpdateDto(User user);
}
