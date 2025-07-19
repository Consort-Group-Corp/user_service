package uz.consortgroup.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.user.request.NotificationCreateRequestDto;
import uz.consortgroup.userservice.service.notification.UserNotificationService;

@RestController
@RequestMapping("/api/v1/user-notifications")
@RequiredArgsConstructor
@Validated
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void sendNotification(@Valid @RequestBody NotificationCreateRequestDto request) {
        userNotificationService.notifyUser(request);
    }
}
