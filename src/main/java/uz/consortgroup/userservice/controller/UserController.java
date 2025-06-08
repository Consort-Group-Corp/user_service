package uz.consortgroup.userservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.core.api.v1.dto.user.request.UserProfileRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserRegistrationRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserProfileResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserRegistrationResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserUpdateResponseDto;
import uz.consortgroup.userservice.service.proxy.order.CourseOrderProxyService;
import uz.consortgroup.userservice.service.user.UserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class UserController {
    private final UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserRegistrationResponseDto registerUser(@RequestBody @Valid UserRegistrationRequestDto userRegistrationRequestDto) {
        return userService.registerNewUser(userRegistrationRequestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{userId}/verification")
    public String verifyUser(@PathVariable UUID userId,
                             @RequestParam @NotBlank(message = "Verification code is required") String verificationCode) {
        userService.verifyUser(userId, verificationCode);
        return "User verified successfully";
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{userId}/new-verification-code")
    public String resendVerificationCode(@PathVariable UUID userId) {
        userService.resendVerificationCode(userId);
        return "Verification code resent successfully";
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{userId}/profile")
    public UserProfileResponseDto fillUserProfile(@PathVariable UUID userId, @RequestBody @Valid UserProfileRequestDto userProfileRequestDto) {
        return userService.fillUserProfile(userId, userProfileRequestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}")
    public UserProfileResponseDto getUserById(@PathVariable("userId") UUID userId) {
       return userService.getUserById(userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{userId}")
    public UserUpdateResponseDto updateUserById(@PathVariable("userId") UUID userId,
                                                @RequestBody @Valid UserUpdateRequestDto userUpdateRequestDto) {
        return userService.updateUserById(userId, userUpdateRequestDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable("userId") UUID userId) {
        userService.deleteUserById(userId);
    }
}
