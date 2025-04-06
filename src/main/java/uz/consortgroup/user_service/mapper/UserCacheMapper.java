package uz.consortgroup.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.user_service.entity.User;
import uz.consortgroup.user_service.entity.cacheEntity.UserCacheEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserCacheMapper {
    UserCacheEntity toUserCache(User user);
    User toUserEntity(UserCacheEntity cacheEntity);
}
