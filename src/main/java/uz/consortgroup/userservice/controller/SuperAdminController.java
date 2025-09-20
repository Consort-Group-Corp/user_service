package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserChangeRequestDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserCreateDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserResponseDto;
import uz.consortgroup.userservice.handler.ErrorResponse;
import uz.consortgroup.userservice.service.super_admin.SuperAdminService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/super-admin")
@Validated
@Tag(name = "Super Admin", description = "Администрирование пользователей (роль, создание)")
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminController {
    private final SuperAdminService superAdminService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(
            value = "/new-role",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "superAdminChangeUserRole",
            summary = "Изменить роль пользователя по email",
            description = "Находит пользователя по email и меняет ему роль на указанную."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Роль изменена",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class),
                            examples = @ExampleObject(value = """
                {
                  "id": "3a7a8b02-9a1b-4e7d-9c0a-1b2c3d4e5f60",
                  "language": "ru",
                  "lastName": "Иванов",
                  "firstName": "Иван",
                  "middleName": "Иванович",
                  "bornDate": "1990-05-20",
                  "phoneNumber": "+998901234567",
                  "workPlace": "Consort Group",
                  "email": "user@example.com",
                  "position": "Mentor",
                  "pinfl": "12345678901234",
                  "role": "MENTOR"
                }
                """))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserResponseDto findUserByEmailAndChangeUserRole(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Email и новая роль пользователя",
                    content = @Content(schema = @Schema(implementation = UserChangeRequestDto.class),
                            examples = @ExampleObject(value = """
                {"email": "user@example.com", "newRole": "MENTOR"}
                """))
            )
            @RequestBody @Valid UserChangeRequestDto userChangeRequestDto
    ) {
        return superAdminService.findUserByEmailAndChangeUserRole(userChangeRequestDto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(
            value = "/new-user",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "superAdminCreateUser",
            summary = "Создать нового пользователя",
            description = "Создает пользователя с указанными данными. По умолчанию может назначаться роль Mentor (зависит от бизнес-логики сервиса)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь создан",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class),
                            examples = @ExampleObject(value = """
                {
                  "id": "3a7a8b02-9a1b-4e7d-9c0a-1b2c3d4e5f60",
                  "language": "ru",
                  "lastName": "Иванов",
                  "firstName": "Иван",
                  "middleName": "Иванович",
                  "bornDate": "1990-05-20",
                  "phoneNumber": "+998901234567",
                  "workPlace": "Consort Group",
                  "email": "new.user@example.com",
                  "position": "Mentor",
                  "pinfl": "12345678901234",
                  "role": "MENTOR"
                }
                """))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserResponseDto createNewUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Данные нового пользователя",
                    content = @Content(schema = @Schema(implementation = UserCreateDto.class),
                            examples = @ExampleObject(value = """
                {
                  "language": "ru",
                  "lastName": "Иванов",
                  "firstName": "Иван",
                  "middleName": "Иванович",
                  "bornDate": "1990-05-20",
                  "phoneNumber": "+998901234567",
                  "workPlace": "Consort Group",
                  "email": "new.user@example.com",
                  "position": "Mentor",
                  "pinfl": "12345678901234",
                  "password": "Abcdef12",
                  "role": "MENTOR"
                }
                """))
            )
            @Valid @RequestBody UserCreateDto userCreateDto
    ) {
        return superAdminService.createNewUser(userCreateDto);
    }
}
