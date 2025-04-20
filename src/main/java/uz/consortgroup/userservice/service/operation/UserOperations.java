package uz.consortgroup.userservice.service.operation;

import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.enumeration.UserRole;

import java.util.UUID;

public interface UserOperations {
    User findUserById(UUID userId);
    User findUserByEmail(String email);
    void saveUser(User user);
    User changeUserRoleByEmail(String email, UserRole role);
}
