package uz.consortgroup.user_service.controller;

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
import uz.consortgroup.user_service.dto.UserRegistrationDto;

import uz.consortgroup.user_service.dto.UserResponseDto;
import uz.consortgroup.user_service.dto.UserUpdateDto;
import uz.consortgroup.user_service.dto.UserUpdateResponseDto;
import uz.consortgroup.user_service.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class UserController {
    private final UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserResponseDto registerNewUser(@RequestBody @Valid UserRegistrationDto userRegistrationDto) {
        return userService.registerNewUser(userRegistrationDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{userId}/verify")
    public String verifyUser(@PathVariable Long userId,
                             @RequestParam @NotBlank(message = "Verification code is required") String verificationCode) {
        userService.verifyUser(userId, verificationCode);
        return "User verified successfully";
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{userId}/new-verification-code")
    public String resendVerificationCode(@PathVariable Long userId) {
        userService.resendVerificationCode(userId);
        return "Verification code resent successfully";
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}")
    public UserResponseDto getUserById(@PathVariable("userId") Long userId) {
       return userService.getUserById(userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{userId}")
    public UserUpdateResponseDto updateUserById(@PathVariable("userId") Long userId,
                                                @RequestBody @Valid UserUpdateDto userUpdateDto) {
        return userService.updateUserById(userId, userUpdateDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable("userId") Long userId) {
        userService.deleteUserById(userId);
    }
}
