package uz.consortgroup.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.userservice.dto.UserResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.UserCacheEntity;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserCacheMapper {
    UserCacheEntity toUserEntity(User user);
    User toUserCache(UserCacheEntity userCacheEntity);
    UserResponseDto toDto(UserCacheEntity userCacheEntity);
}
