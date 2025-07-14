package uz.consortgroup.userservice.service.strategy;

import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserSearchLoggerStrategyFactory {
    private final Map<UserRole, UserSearchActionLoggerStrategy> strategyMap;

    public UserSearchLoggerStrategyFactory(List<UserSearchActionLoggerStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(UserSearchActionLoggerStrategy::getSupportedRole, Function.identity()));
    }

    @AllAspect
    public Optional<UserSearchActionLoggerStrategy> get(UserRole role) {
        return Optional.ofNullable(strategyMap.get(role));
    }
}
