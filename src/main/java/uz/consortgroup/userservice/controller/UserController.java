package uz.consortgroup.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.userservice.dto.UserRegistrationDto;

import uz.consortgroup.userservice.dto.UserResponseDto;
import uz.consortgroup.userservice.dto.UserUpdateDto;
import uz.consortgroup.userservice.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> registerNewUser(@RequestBody @Valid UserRegistrationDto userRegistrationDto) {
        return ResponseEntity.ok(userService.registerNewUser(userRegistrationDto));
    }

    @PostMapping("/{userId}/verify")
    public ResponseEntity<String> verifyUser(@PathVariable Long userId, @RequestParam String verificationCode) {
        userService.verifyUser(userId, verificationCode);
        return ResponseEntity.ok("User verified successfully");
    }

    @PostMapping("/{userId}/resend-verification-code")
    public ResponseEntity<String> resendVerificationCode(@PathVariable Long userId) {
        userService.resendVerificationCode(userId);
        return ResponseEntity.ok("Verification code resent successfully");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("userId") Long userId) {
       return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserUpdateDto> updateUserById(@PathVariable("userId") Long userId,
                                                        @RequestBody @Valid UserUpdateDto userUpdateDto) {
        return ResponseEntity.ok(userService.updateUserById(userId, userUpdateDto));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("userId") Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.noContent().build();
    }
}
