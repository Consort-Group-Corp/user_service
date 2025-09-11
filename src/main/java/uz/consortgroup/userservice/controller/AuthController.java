package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.user.auth.JwtResponse;
import uz.consortgroup.core.api.v1.dto.user.auth.LoginRequest;
import uz.consortgroup.userservice.handler.ErrorResponse;
import uz.consortgroup.userservice.service.auth.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authorization", description = "Аутентификация и авторизация пользователей")
@Validated
public class AuthController {

    private final AuthService authService;

    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirements(value = {})
    @Operation(
            summary = "Аутентификация пользователя",
            description = "Авторизация по языку интерфейса, электронной почте и паролю. "
                    + "В случае успешной аутентификации возвращается JWT-токен и роль пользователя."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                              "role": "ADMIN"
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-08-13T12:00:00",
                              "status": 400,
                              "error": "Validation failed",
                              "message": "Email is not valid; Password cannot be empty"
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный логин или пароль",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-08-13T12:00:00",
                              "status": 401,
                              "error": "Authentication failed",
                              "message": "Invalid email or password"
                            }
                            """)
                    )
            )
    })
    @PostMapping("/login")
    public JwtResponse authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }


    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirements(value = {})
    @Operation(
            summary = "Аутентификация супер админа",
            description = "Авторизация супер-администратора по языку интерфейса, электронной почте и паролю."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                              "role": "SUPER_ADMIN"
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный логин или пароль",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/super-admin/login")
    public JwtResponse authenticateSuperAdmin(@RequestBody @Valid LoginRequest loginRequest) {
        return authService.authenticateSuperAdmin(loginRequest);
    }
}
