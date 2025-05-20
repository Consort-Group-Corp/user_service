package uz.consortgroup.userservice.service.verification;

import uz.consortgroup.userservice.entity.User;

public interface VerificationService {
    String generateAndSaveCode(User user);
    void verifyCode(User user, String inputCode);
}
