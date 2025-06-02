package uz.consortgroup.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.consortgroup.core.api.v1.dto.user.auth.JwtResponse;
import uz.consortgroup.userservice.service.one_id.OneIdService;

@RestController
@RequestMapping("/api/v1/oneid")
@RequiredArgsConstructor
public class OneIdController {

    private final OneIdService oneIdService;

    @GetMapping("/login-url")
    public ResponseEntity<String> getLoginUrl() {
        return ResponseEntity.ok(oneIdService.buildAuthUrl());
    }

    @GetMapping("/callback")
    public ResponseEntity<JwtResponse> handleCallback(@RequestParam String code) {
        return ResponseEntity.ok(oneIdService.authorizeViaOneId(code));
    }
}
