package uz.consortgroup.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.core.api.v1.dto.forum.HrForumGroupCreateResponse;
import uz.consortgroup.userservice.entity.ForumUserGroup;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ForumGroupMapper {
    @Mapping(target = "groupId", source = "group.id")
    HrForumGroupCreateResponse toResponseDto(ForumUserGroup group);
}
