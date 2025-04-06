package uz.consortgroup.user_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import uz.consortgroup.user_service.service.OneIdService;

@RestController
@RequiredArgsConstructor
public class OneIdAuthController {
    private final OneIdService oneIdService;

    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        return ResponseEntity.status(302).location(oneIdService.generateAuthUrl()).build();
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<?>> oneIdCallback(@RequestParam("code") String code) {
        return oneIdService.processCallback(code);
    }
}
