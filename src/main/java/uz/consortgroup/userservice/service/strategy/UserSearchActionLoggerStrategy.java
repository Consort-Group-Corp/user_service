package uz.consortgroup.userservice.service.strategy;

import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;

import java.util.UUID;

public interface UserSearchActionLoggerStrategy {
    void log(UUID actorId, User targetUser);
    UserRole getSupportedRole();
}
