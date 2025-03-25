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
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.userservice.dto.UserCreateDto;

import uz.consortgroup.userservice.dto.UserResponseDto;
import uz.consortgroup.userservice.dto.UserUpdateDto;
import uz.consortgroup.userservice.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserCreateDto> registerNewUser(@RequestBody @Valid UserCreateDto userCreateDto) {
        return ResponseEntity.ok(userService.registerNewUser(userCreateDto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserResponseDto> findUserById(@PathVariable("userId") Long userId) {
       return ResponseEntity.ok(userService.findUserById(userId));
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<UserUpdateDto> updateUserById(@PathVariable("userId") Long userId, @RequestBody @Valid UserUpdateDto userUpdateDto) {
        return ResponseEntity.ok(userService.updateUserById(userId, userUpdateDto));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("userId") Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.noContent().build();
    }
}
