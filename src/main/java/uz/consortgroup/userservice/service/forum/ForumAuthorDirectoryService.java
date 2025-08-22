package uz.consortgroup.userservice.service.forum;

import uz.consortgroup.core.api.v1.dto.forum.ForumAuthorDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ForumAuthorDirectoryService {
    Map<UUID, ForumAuthorDto> getAuthors(List<UUID> ids);
}
