package uz.consortgroup.userservice.service.user;

import uz.consortgroup.core.api.v1.dto.user.request.UserProfileRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserRegistrationRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserProfileResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserRegistrationResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserUpdateResponseDto;

import java.util.UUID;

public interface UserService {
    UserRegistrationResponseDto registerNewUser(UserRegistrationRequestDto userRegistrationRequestDto);
    void verifyUser(UUID userId, String inputCode);
    void resendVerificationCode(UUID userId);
    UserProfileResponseDto fillUserProfile(UUID userId, UserProfileRequestDto userProfileRequestDto);
    UserProfileResponseDto getUserById(UUID userId);
    UserUpdateResponseDto updateUserById(UUID userId, UserUpdateRequestDto updateDto);
    void deleteUserById(UUID id);
}
