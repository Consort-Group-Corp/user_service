package uz.consortgroup.userservice.service.operation;

import uz.consortgroup.userservice.entity.User;

import java.util.UUID;

public interface UserOperations {
    User findUserById(UUID userId);
}