package uz.consortgroup.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.userservice.dto.UserRegistrationDto;
import uz.consortgroup.userservice.dto.UserResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.UserCacheEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);
}
