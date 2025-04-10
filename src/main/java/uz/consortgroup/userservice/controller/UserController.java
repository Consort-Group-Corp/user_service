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
import uz.consortgroup.userservice.dto.UserProfileDto;

import uz.consortgroup.userservice.dto.UserProfileResponseDto;
import uz.consortgroup.userservice.dto.UserRegistrationDto;
import uz.consortgroup.userservice.dto.UserRegistrationResponseDto;
import uz.consortgroup.userservice.dto.UserUpdateDto;
import uz.consortgroup.userservice.dto.UserUpdateResponseDto;
import uz.consortgroup.userservice.service.UserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class UserController {
    private final UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserRegistrationResponseDto registerUser(@RequestBody @Valid UserRegistrationDto userRegistrationDto) {
        return userService.registerNewUser(userRegistrationDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{userId}/verify")
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
    @PostMapping("/{userId}/profile")
    public UserProfileResponseDto fillUserProfile(@PathVariable UUID userId, @RequestBody @Valid UserProfileDto userProfileDto) {
        return userService.fillUserProfile(userId, userProfileDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}")
    public UserProfileResponseDto getUserById(@PathVariable("userId") UUID userId) {
       return userService.getUserById(userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{userId}")
    public UserUpdateResponseDto updateUserById(@PathVariable("userId") UUID userId,
                                                @RequestBody @Valid UserUpdateDto userUpdateDto) {
        return userService.updateUserById(userId, userUpdateDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable("userId") UUID userId) {
        userService.deleteUserById(userId);
    }
}
