package uz.consortgroup.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.userservice.entity.SuperAdmin;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SuperAdminMapper {

    @Mapping(target = "role", source = "role")
    UserShortInfoResponseDto toShortInfoDto(SuperAdmin entity);
}
