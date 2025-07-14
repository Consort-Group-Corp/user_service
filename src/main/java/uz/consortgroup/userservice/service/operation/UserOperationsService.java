package uz.consortgroup.userservice.service.operation;

import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.userservice.entity.User;

import java.util.UUID;

public interface UserOperationsService {
    User findUserById(UUID userId);
    User findUserByEmail(String email);
    void saveUser(User user);
    User changeUserRoleByEmail(String email, UserRole role);
    UUID findUserIdByEmail(String email);
    User getUserFromDbAndCacheById(UUID userId);
    User findUserByEmailOrPinfl(String query);
}
