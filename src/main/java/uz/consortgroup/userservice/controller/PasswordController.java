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

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/password")
@Tag(name = "Password", description = "Сброс и обновление пароля")
public class PasswordController {

    private final PasswordService passwordService;

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = "/recovery", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
            operationId = "requestPasswordResetForCurrentUser",
            summary = "Запросить сброс пароля (текущий пользователь)",
            description = "Отправляет на e-mail текущего авторизованного пользователя ссылку для сброса пароля."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Запрос принят",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Password reset request sent"))),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Слишком много попыток",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public String requestForCurrentUser() {
        passwordService.requestPasswordResetForCurrentUser();
        return "Password reset request sent";
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = "/recovery/anonymous", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
            operationId = "requestPasswordResetByEmail",
            summary = "Запросить сброс пароля (по e-mail, без авторизации)",
            description = "Принимает e-mail в теле запроса. Всегда возвращает 202, чтобы не раскрывать существование аккаунта."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Запрос принят",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Password reset request accepted"))),
            @ApiResponse(responseCode = "429", description = "Слишком много попыток",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public String requestAnonymous(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "E-mail пользователя",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            { "email": "user@example.com" }
                            """))
            )
            @RequestBody Map<String, String> body
    ) {
        passwordService.requestPasswordResetByEmail(body.getOrDefault("email", ""));
        return "Password reset request accepted";
    }

    @PutMapping(
            value = "/new-password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @Operation(
            operationId = "updatePassword",
            summary = "Установить новый пароль",
            description = "Подтверждает сброс пароля по reset-токену и устанавливает новый пароль пользователю."
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
                    description = "Reset-токен из письма",
                    example = "eyJhbGciOiJIUzI1NiJ9..."
            )
            @RequestParam String token
    ) {
        passwordService.updatePassword(request, token);
        return "Password updated successfully";
    }
}
