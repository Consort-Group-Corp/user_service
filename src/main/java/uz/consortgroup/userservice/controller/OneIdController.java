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
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.consortgroup.core.api.v1.dto.user.auth.JwtResponse;
import uz.consortgroup.userservice.handler.ErrorResponse;
import uz.consortgroup.userservice.service.one_id.OneIdService;

@RestController
@RequestMapping("/api/v1/oneid")
@RequiredArgsConstructor
@Tag(name = "OneId", description = "Регистрация/вход через OneID (SSO РУз)")
public class OneIdController {

    private final OneIdService oneIdService;

    @GetMapping(value = "/login-url", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
            operationId = "oneIdGetLoginUrl",
            summary = "Получить ссылку авторизации OneID",
            description = "Возвращает готовый URL на страницу авторизации OneID с нужными client_id, redirect_uri и scope. "
                    + "Клиент должен перенаправить пользователя по этой ссылке."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ссылка сгенерирована",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value =
                                    "https://sso.egov.uz/sso/oauth/Authorization.do"
                                            + "?response_type=one_code"
                                            + "&client_id=CLIENT_ID"
                                            + "&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fv1%2Foneid%2Fcallback"
                                            + "&scope=profile"))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> getLoginUrl() {
        return ResponseEntity.ok(oneIdService.buildAuthUrl());
    }

    @GetMapping(value = "/callback", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "oneIdCallback",
            summary = "Callback от OneID",
            description = "Обработка редиректа после успешной авторизации на OneID. "
                    + "Принимает короткоживущий авторизационный код и выдает JWT для вашего приложения."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная авторизация",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class),
                            examples = @ExampleObject(value = """
                {
                  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                  "role": "STUDENT"
                }
                """))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос/код",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                {
                  "timestamp": "2025-08-13T19:54:12",
                  "status": 400,
                  "error": "Validation failed",
                  "message": "Missing or invalid 'code' parameter"
                }
                """))),
            @ApiResponse(responseCode = "401", description = "Не удалось авторизоваться в OneID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                {
                  "timestamp": "2025-08-13T19:55:01",
                  "status": 401,
                  "error": "Authentication failed",
                  "message": "OneID authorization failed"
                }
                """))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<JwtResponse> handleCallback(
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "code",
                    required = true,
                    description = "Авторизационный код, полученный от OneID после согласия пользователя. "
                            + "Одноразовый, короткоживущий.",
                    example = "SplxlOBeZQQYbYS6WxSbIA"
            )
            @RequestParam String code
    ) {
        return ResponseEntity.ok(oneIdService.authorizeViaOneId(code));
    }
}
