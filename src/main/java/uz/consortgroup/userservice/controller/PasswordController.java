package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uz.consortgroup.core.api.v1.dto.user.request.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.handler.ErrorResponse;
import uz.consortgroup.userservice.service.password.PasswordService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/password")
@Tag(name = "Password", description = "Сброс и обновление пароля")
public class PasswordController {

    private final PasswordService passwordService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{userId}/recovery", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
            operationId = "requestPasswordReset",
            summary = "Запросить сброс пароля",
            description = "Отправляет пользователю код/ссылку для сброса пароля (e-mail/SMS/уведомление в приложении)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Запрос на сброс пароля принят",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Password reset request sent successfully"))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Слишком много попыток (rate limit)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public String resetPassword(
            @Parameter(
                    in = ParameterIn.PATH,
                    name = "userId",
                    required = true,
                    description = "Идентификатор пользователя, для которого инициируется сброс пароля",
                    example = "e2f00427-fd76-41ad-940e-4624955f9384"
            )
            @PathVariable UUID userId
    ) {
        passwordService.requestPasswordReset(userId);
        return "Password reset request sent successfully";
    }

    @PutMapping(
            value = "/{userId}/new-password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @Operation(
            operationId = "updatePassword",
            summary = "Установить новый пароль",
            description = "Подтверждает сброс пароля по токену и устанавливает новый пароль пользователю."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль успешно обновлён",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Password updated successfully"))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/несовпадение паролей",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                {
                  "timestamp": "2025-08-13T19:54:12",
                  "status": 400,
                  "error": "Validation failed",
                  "message": "Passwords do not match"
                }
                """))),
            @ApiResponse(responseCode = "401", description = "Неверный или истёкший токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                {
                  "timestamp": "2025-08-13T19:55:01",
                  "status": 401,
                  "error": "Authentication failed",
                  "message": "Reset token is invalid or expired"
                }
                """))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public String updatePassword(
            @Parameter(
                    in = ParameterIn.PATH,
                    name = "userId",
                    required = true,
                    description = "Идентификатор пользователя, чей пароль обновляется",
                    example = "e2f00427-fd76-41ad-940e-4624955f9384"
            )
            @PathVariable UUID userId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Новый пароль и его подтверждение",
                    content = @Content(schema = @Schema(implementation = UpdatePasswordRequestDto.class),
                            examples = @ExampleObject(value = """
                {
                  "newPassword": "N3wP@ssw0rd!",
                  "confirmPassword": "N3wP@ssw0rd!"
                }
                """))
            )
            @RequestBody @Valid UpdatePasswordRequestDto request,

            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "token",
                    required = true,
                    description = "Токен сброса пароля, присланный пользователю (из ссылки/письма)",
                    example = "f9b1c4e1-2d3a-4b5c-8a9f-0123456789ab"
            )
            @RequestParam String token
    ) {
        passwordService.updatePassword(userId, request, token);
        return "Password updated successfully";
    }
}
