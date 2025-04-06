package uz.consortgroup.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserCacheMapper {
    UserCacheEntity toUserCache(User user);
    User toUserEntity(UserCacheEntity cacheEntity);
}
