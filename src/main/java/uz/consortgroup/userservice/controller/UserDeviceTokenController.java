package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.consortgroup.core.api.v1.dto.user.request.RegisterDeviceTokenRequest;
import uz.consortgroup.core.api.v1.dto.user.response.FcmTokenDto;
import uz.consortgroup.userservice.handler.ErrorResponse;
import uz.consortgroup.userservice.service.device.UserDeviceTokenService;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/device-tokens")
@RequiredArgsConstructor
@Tag(name = "Device tokens", description = "Регистрация и получение FCM-токенов устройств")
@SecurityRequirement(name = "bearerAuth")
public class UserDeviceTokenController {

    private final UserDeviceTokenService userDeviceTokenService;

    // -------- Register token --------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            operationId = "registerDeviceToken",
            summary = "Зарегистрировать (или обновить) FCM-токен устройства",
            description = "Привязывает FCM-токен к текущему пользователю. Если такой токен уже существует — обновляет метаданные."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Токен зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void registerToken(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true, description = "Данные FCM-токена",
                    content = @Content(
                            schema = @Schema(implementation = RegisterDeviceTokenRequest.class),
                            examples = @ExampleObject(value = """
                {
                  "fcmToken": "fcm_abcdef123456",
                  "deviceType": "ANDROID",
                  "language": "ru"
                }
                """)
                    )
            )
            @RequestBody @Valid RegisterDeviceTokenRequest request
    ) {
        UUID userId = userDetails.getId();
        userDeviceTokenService.registerToken(userId, request);
    }

    // -------- Get current user's tokens (paged) --------
    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            operationId = "getMyDeviceTokens",
            summary = "Получить активные токены текущего пользователя (постранично)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Успех",
                    content = @Content(mediaType = "application/json",
                            // показываем пример Page-ответа
                            examples = @ExampleObject(value = """
                {
                  "content": [
                    {
                      "userId": "8a0b7d1a-0f74-4a7d-8c8d-0e9f0b1c2d3e",
                      "language": "ru",
                      "fcmToken": "fcm_abcdef123456",
                      "deviceType": "ANDROID"
                    }
                  ],
                  "pageable": {"pageNumber": 0, "pageSize": 20},
                  "totalElements": 1,
                  "totalPages": 1,
                  "last": true,
                  "size": 20,
                  "number": 0,
                  "sort": {"empty": true, "unsorted": true, "sorted": false},
                  "first": true,
                  "numberOfElements": 1,
                  "empty": false
                }
                """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Page<FcmTokenDto> getUserTokens(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(in = ParameterIn.QUERY, description = "Номер страницы (0..N)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(in = ParameterIn.QUERY, description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = userDetails.getId();
        return userDeviceTokenService.getActiveTokensByUserId(userId, page, size);
    }

    // -------- Get all active tokens (paged) --------
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            operationId = "getAllActiveDeviceTokens",
            summary = "Получить все активные токены (постранично)",
            description = "Требует прав администратора/сервисного пользователя."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Успех",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(ref = "#/components/examples/FcmTokenPageExample")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Page<FcmTokenDto> getAllActiveTokens(
            @Parameter(in = ParameterIn.QUERY, description = "Номер страницы (0..N)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(in = ParameterIn.QUERY, description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return userDeviceTokenService.getActiveTokensPaged(page, size);
    }

    // -------- Get tokens by user IDs --------
    @PostMapping("/by-user-ids")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            operationId = "getTokensByUserIds",
            summary = "Получить токены для списка пользователей",
            description = "Возвращает Map: ключ — UUID пользователя, значение — список его FCM-токенов."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Успех",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object",
                                    description = "Словарь userId → список токенов"),
                            examples = @ExampleObject(value = """
                {
                  "8a0b7d1a-0f74-4a7d-8c8d-0e9f0b1c2d3e": [
                    {"userId":"8a0b...","language":"ru","fcmToken":"fcm_1","deviceType":"ANDROID"},
                    {"userId":"8a0b...","language":"ru","fcmToken":"fcm_2","deviceType":"WEB"}
                  ],
                  "7bb3a21c-6b1e-4a99-9b4b-0d1a2c3e4f5a": [
                    {"userId":"7bb3...","language":"uz","fcmToken":"fcm_3","deviceType":"IOS"}
                  ]
                }
                """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Map<UUID, List<FcmTokenDto>> getTokensByUserIds(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true, description = "Список идентификаторов пользователей",
                    content = @Content(array = @ArraySchema(schema = @Schema(format = "uuid")),
                            examples = @ExampleObject(value = """
                ["8a0b7d1a-0f74-4a7d-8c8d-0e9f0b1c2d3e","7bb3a21c-6b1e-4a99-9b4b-0d1a2c3e4f5a"]
                """))
            )
            @RequestBody List<UUID> userIds
    ) {
        return userDeviceTokenService.getTokensByUserIds(userIds);
    }
}
