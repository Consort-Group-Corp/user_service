package uz.consortgroup.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.request.UserBulkSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.request.UserSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserBulkSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.security.AuthContext;
import uz.consortgroup.userservice.service.operation.UserOperationsService;
import uz.consortgroup.userservice.service.strategy.UserSearchLoggerStrategyFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSearchServiceImpl implements UserSearchService {
    private final UserOperationsService userOperationService;
    private final UserMapper userMapper;
    private final UserSearchLoggerStrategyFactory strategyFactory;
    private final AuthContext authContext;

    @Transactional(readOnly = true)
    @Override
    public UserSearchResponse findUserByEmailOrPinfl(UserSearchRequest dto) {
        log.info("Searching user by query: {}", dto.getQuery());

        User foundUser = userOperationService.findUserByEmailOrPinfl(dto.getQuery());

        UserRole role = authContext.getActorRole();
        UUID actorId = authContext.getActorId();

        strategyFactory.get(role).ifPresent(strategy -> strategy.log(actorId, foundUser));

        log.info("User found: id={}, searchedByRole={}", foundUser.getId(), role);
        return userMapper.toUserSearchResponse(foundUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserBulkSearchResponse bulkUserSearch(UserBulkSearchRequest dto) {
        log.info("Searching users by {} queries", dto.getQueries().size());

        Map<Boolean, List<String>> partitioned = dto.getQueries().stream()
                .map(UserSearchRequest::getQuery)
                .filter(Objects::nonNull)
                .collect(Collectors.partitioningBy(this::isEmail));

        List<String> emails = partitioned.get(true);
        List<String> pinfls = partitioned.get(false).stream()
                .filter(this::isPinfl)
                .toList();

        List<User> users = userOperationService.findUsersBatch(emails, pinfls);
        log.debug("Bulk search queries: {}", dto.getQueries().stream().map(UserSearchRequest::getQuery).toList());

        return userMapper.toUserBulkSearchResponse(users);
    }

    private boolean isEmail(String query) {
        return query.contains("@");
    }

    private boolean isPinfl(String query) {
        return query.matches("\\d{14}");
    }
}
