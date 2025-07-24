package uz.consortgroup.userservice.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserSearchLoggerStrategyFactory {
    private final Map<UserRole, UserSearchActionLoggerStrategy> strategyMap;

    public UserSearchLoggerStrategyFactory(List<UserSearchActionLoggerStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(UserSearchActionLoggerStrategy::getSupportedRole, Function.identity()));
    }

    public Optional<UserSearchActionLoggerStrategy> get(UserRole role) {
        Optional<UserSearchActionLoggerStrategy> strategy = Optional.ofNullable(strategyMap.get(role));

        if (strategy.isPresent()) {
            log.debug("Found strategy for role: {}", role);
        } else {
            log.warn("Not found strategy for role: {}", role);
        }

        return strategy;
    }
}
