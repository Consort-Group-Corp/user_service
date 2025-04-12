package uz.consortgroup.userservice.service.operation;

import uz.consortgroup.userservice.entity.User;

public interface PasswordOperations {
    void savePassword(User user, String rawPassword);
}