package uz.consortgroup.userservice.service.password;

import uz.consortgroup.core.api.v1.dto.user.request.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.entity.User;

public interface PasswordService {
    void savePassword(User user, String rawPassword);
    void requestPasswordResetForCurrentUser();
    void updatePassword(UpdatePasswordRequestDto request, String token);
    void requestPasswordResetByEmail(String email);
}