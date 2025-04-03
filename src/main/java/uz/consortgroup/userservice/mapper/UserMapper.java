package uz.consortgroup.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.userservice.dto.UserResponseDto;
import uz.consortgroup.userservice.dto.UserUpdateResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.enumeration.UserRole;
import uz.consortgroup.userservice.entity.enumeration.UserStatus;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);

    UserUpdateResponseDto toUserUpdateResponseDto(User user);
}
