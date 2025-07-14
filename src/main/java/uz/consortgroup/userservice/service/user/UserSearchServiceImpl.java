package uz.consortgroup.userservice.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.request.UserSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.security.AuthContext;
import uz.consortgroup.userservice.service.operation.UserOperationsService;
import uz.consortgroup.userservice.service.strategy.UserSearchLoggerStrategyFactory;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {
    private final UserOperationsService userOperationService;
    private final UserMapper userMapper;
    private final UserSearchLoggerStrategyFactory strategyFactory;
    private final AuthContext authContext;

    @Transactional(readOnly = true)
    @Override
    @AllAspect
    public UserSearchResponse findUserByEmailOrPinfl(UserSearchRequest dto) {
        User foundUser = userOperationService.findUserByEmailOrPinfl(dto.getQuery());

        UserRole role = authContext.getActorRole();
        UUID actorId  = authContext.getActorId();

        strategyFactory.get(role).ifPresent(strategy -> strategy.log(actorId, foundUser));

        return userMapper.toUserSearchResponse(foundUser);
    }
}
