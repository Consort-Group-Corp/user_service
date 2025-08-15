package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.consortgroup.core.api.v1.dto.user.request.NotificationCreateRequestDto;
import uz.consortgroup.userservice.handler.ErrorResponse;
import uz.consortgroup.userservice.service.notification.UserNotificationService;

@RestController
@RequestMapping("/api/v1/user-notifications")
@RequiredArgsConstructor
@Validated
@Tag(name = "Notifications", description = "Отправка пользовательских уведомлений")
@SecurityRequirement(name = "bearerAuth")
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            operationId = "sendUserNotification",
            summary = "Отправить/запланировать уведомление пользователям",
            description = "Создаёт задачу на немедленную отправку или планирование сообщения (PUSH/SMS/EMAIL) указанным пользователям. "
                    + "Локализованные тексты передаются в виде словаря: язык → {title, message}."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Создано (уведомление принято к обработке)"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void sendNotification(
            @RequestBody(
                    required = true,
                    description = "Параметры рассылки (отправителя, канал, получатели, тексты и, опционально, время отправки)",
                    content = @Content(
                            schema = @Schema(implementation = NotificationCreateRequestDto.class),
                            examples = @ExampleObject(name = "Push RU/EN сейчас",
                                    value = """
                    {
                      "createdByUserId": "b51f75f9-5a0a-4a66-9f2f-2a0f8b9d1a23",
                      "creatorRole": "ADMIN",
                      "communication": "PUSH",
                      "sendAt": "2025-08-13T23:45:00",
                      "active": true,
                      "recipientUserIds": [
                        "8a0b7d1a-0f74-4a7d-8c8d-0e9f0b1c2d3e",
                        "7bb3a21c-6b1e-4a99-9b4b-0d1a2c3e4f5a"
                      ],
                      "translations": {
                        "ru": {
                          "title": "Новый урок доступен",
                          "message": "Откройте приложение, чтобы посмотреть материал"
                        },
                        "en": {
                          "title": "New lesson available",
                          "message": "Open the app to watch the content"
                        }
                      }
                    }
                    """)
                    )
            )
            @Valid  NotificationCreateRequestDto request
    ) {
        userNotificationService.notifyUser(request);
    }
}
