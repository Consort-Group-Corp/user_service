package uz.consortgroup.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.user.request.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.service.password.PasswordService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/password")
public class PasswordController {
    private final PasswordService passwordService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{userId}/recovery")
    public String resetPassword(@PathVariable UUID userId) {
        passwordService.requestPasswordReset(userId);
        return "Password reset request sent successfully";
    }

    @PutMapping("/{userId}/new-password")
    public String updatePassword(@PathVariable UUID userId,
                                 @Valid @RequestBody UpdatePasswordRequestDto request, @RequestParam String token) {
        passwordService.updatePassword(userId, request, token);
        return "Password updated successfully";
    }
}
