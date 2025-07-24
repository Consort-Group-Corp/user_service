package uz.consortgroup.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.request.UserSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.security.AuthContext;
import uz.consortgroup.userservice.service.operation.UserOperationsService;
import uz.consortgroup.userservice.service.strategy.UserSearchLoggerStrategyFactory;

import java.util.UUID;

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
}
