package uz.consortgroup.userservice.service.forum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.forum.ForumAuthorDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.service.operation.UserOperationsService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumAuthorDirectoryServiceImpl implements ForumAuthorDirectoryService {
    private final UserOperationsService userOperationsService;

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, ForumAuthorDto> getAuthors(List<UUID> ids) {
        log.info("Fetching authors by ids: {}", ids);

        if (ids == null || ids.isEmpty()) {
            log.debug("Received empty or null list of IDs, returning empty map");
            return Map.of();
        }

        log.debug("Processing {} author IDs", ids.size());

        try {
            List<User> users = userOperationsService.batchFindUsersById(ids);
            log.debug("Successfully retrieved {} users from userOperationsService", users.size());

            Map<UUID, ForumAuthorDto> result = users.stream()
                    .collect(Collectors.toMap(
                            User::getId,
                            user -> {
                                log.trace("Mapping user {} to ForumAuthorDto", user.getId());
                                return ForumAuthorDto.builder()
                                        .id(user.getId())
                                        .lastName(user.getLastName())
                                        .firstName(user.getFirstName())
                                        .middleName(user.getMiddleName())
                                        .role(user.getRole() == null ? null : user.getRole())
                                        .build();
                            }
                    ));

            log.info("Successfully processed {} authors into DTO map", result.size());
            return result;

        } catch (Exception e) {
            log.error("Error occurred while fetching authors for IDs: {}", ids, e);
            throw e;
        }
    }
}