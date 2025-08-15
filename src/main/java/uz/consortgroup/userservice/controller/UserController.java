package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.consortgroup.core.api.v1.dto.user.request.UserProfileRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserRegistrationRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserProfileResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserRegistrationResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserUpdateResponseDto;
import uz.consortgroup.userservice.handler.ErrorResponse;
import uz.consortgroup.userservice.service.user.UserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
@Tag(name = "Users", description = "Регистрация, верификация и управление профилем пользователя")
public class UserController {
    private final UserService userService;

    // ---------- Регистрация ----------
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(
            operationId = "registerUser",
            summary = "Регистрация пользователя",
            description = "Создает учетную запись по email, языку и паролю."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь зарегистрирован",
                    content = @Content(schema = @Schema(implementation = UserRegistrationResponseDto.class),
                            examples = @ExampleObject(value = """
                {"id":"1dc5e8a1-9c3e-4f1a-9d8e-4a3e8f5c9a12","language":"ru","email":"user@example.com"}
                """))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserRegistrationResponseDto registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true, description = "Данные для регистрации",
                    content = @Content(schema = @Schema(implementation = UserRegistrationRequestDto.class),
                            examples = @ExampleObject(value = """
                {"language":"ru","email":"user@example.com","password":"Abcdef12"}
                """))
            )
            @RequestBody @Valid UserRegistrationRequestDto userRegistrationRequestDto
    ) {
        return userService.registerNewUser(userRegistrationRequestDto);
    }

    // ---------- Верификация ----------
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{userId}/verification")
    @Operation(
            operationId = "verifyUser",
            summary = "Подтвердить аккаунт",
            description = "Проверяет код верификации пользователя."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь верифицирован",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "User verified successfully"))),
            @ApiResponse(responseCode = "400", description = "Неверный/просроченный код",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public String verifyUser(
            @Parameter(in = ParameterIn.PATH, description = "ID пользователя", required = true,
                    example = "8a0b7d1a-0f74-4a7d-8c8d-0e9f0b1c2d3e")
            @PathVariable UUID userId,
            @Parameter(in = ParameterIn.QUERY, description = "Код подтверждения", required = true, example = "123456")
            @RequestParam @NotBlank(message = "Verification code is required") String verificationCode
    ) {
        userService.verifyUser(userId, verificationCode);
        return "User verified successfully";
    }

    // ---------- Повторная отправка кода ----------
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{userId}/new-verification-code")
    @Operation(
            operationId = "resendVerificationCode",
            summary = "Повторно отправить код верификации",
            description = "Генерирует и отправляет новый код подтверждения."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Код отправлен",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Verification code resent successfully"))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public String resendVerificationCode(
            @Parameter(in = ParameterIn.PATH, description = "ID пользователя", required = true,
                    example = "8a0b7d1a-0f74-4a7d-8c8d-0e9f0b1c2d3e")
            @PathVariable UUID userId
    ) {
        userService.resendVerificationCode(userId);
        return "Verification code resent successfully";
    }

    // ---------- Заполнение профиля ----------
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{userId}/profile")
    @Operation(
            operationId = "fillUserProfile",
            summary = "Заполнить профиль",
            description = "Создает/заполняет профиль пользователя.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Профиль создан/заполнен",
                    content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserProfileResponseDto fillUserProfile(
            @Parameter(in = ParameterIn.PATH, description = "ID пользователя", required = true,
                    example = "8a0b7d1a-0f74-4a7d-8c8d-0e9f0b1c2d3e")
            @PathVariable UUID userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true, description = "Данные профиля",
                    content = @Content(schema = @Schema(implementation = UserProfileRequestDto.class),
                            examples = @ExampleObject(value = """
                {
                  "lastName":"Иванов","firstName":"Иван","middleName":"Иванович",
                  "bornDate":"20-05-1990","phoneNumber":"+998901234567",
                  "workPlace":"Consort Group","position":"Mentor","pinfl":"12345678901234"
                }
                """))
            )
            @RequestBody @Valid UserProfileRequestDto userProfileRequestDto
    ) {
        return userService.fillUserProfile(userId, userProfileRequestDto);
    }

    // ---------- Получить пользователя ----------
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}")
    @Operation(
            operationId = "getUserById",
            summary = "Получить пользователя по ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех",
                    content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserProfileResponseDto getUserById(
            @Parameter(in = ParameterIn.PATH, description = "ID пользователя", required = true,
                    example = "8a0b7d1a-0f74-4a7d-8c8d-0e9f0b1c2d3e")
            @PathVariable("userId") UUID userId
    ) {
        return userService.getUserById(userId);
    }

    // ---------- Обновить пользователя ----------
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{userId}")
    @Operation(
            operationId = "updateUserById",
            summary = "Обновить данные пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлено",
                    content = @Content(schema = @Schema(implementation = UserUpdateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserUpdateResponseDto updateUserById(
            @Parameter(in = ParameterIn.PATH, description = "ID пользователя", required = true,
                    example = "8a0b7d1a-0f74-4a7d-8c8d-0e9f0b1c2d3e")
            @PathVariable("userId") UUID userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true, description = "Данные для обновления",
                    content = @Content(schema = @Schema(implementation = UserUpdateRequestDto.class),
                            examples = @ExampleObject(value = """
                {
                  "lastName":"Иванов","firstName":"Иван","middleName":"Иванович",
                  "bornDate":"20-05-1990","phoneNumber":"+998901234567",
                  "workPlace":"Consort Group","email":"user@example.com",
                  "position":"Mentor","pinfl":"12345678901234","role":"MENTOR"
                }
                """))
            )
            @RequestBody @Valid UserUpdateRequestDto userUpdateRequestDto
    ) {
        return userService.updateUserById(userId, userUpdateRequestDto);
    }

    // ---------- Удалить пользователя ----------
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}")
    @Operation(
            operationId = "deleteUserById",
            summary = "Удалить пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Удален"),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void deleteUserById(
            @Parameter(in = ParameterIn.PATH, description = "ID пользователя", required = true,
                    example = "8a0b7d1a-0f74-4a7d-8c8d-0e9f0b1c2d3e")
            @PathVariable("userId") UUID userId
    ) {
        userService.deleteUserById(userId);
    }
}
