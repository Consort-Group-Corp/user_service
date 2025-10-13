package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import uz.consortgroup.core.api.v1.dto.user.response.PageResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserFullInfoResponseDto;
import uz.consortgroup.userservice.common.PageResponses;
import uz.consortgroup.userservice.service.user.UserQueryService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users-full-info")
@Tag(name = "User Query", description = "Получение полной информации о пользователях")
public class UserQueryController {

    private final UserQueryService userQueryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Список пользователей (полная карточка)")
    public PageResponse<UserFullInfoResponseDto> getAllUsersFullInfo(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return PageResponses.from(userQueryService.getAllUsersFullInfo(pageable));
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Пользователь по ID (полная карточка)")
    public UserFullInfoResponseDto getUserFullInfoById(@PathVariable UUID userId) {
        return userQueryService.getUserFullInfoById(userId);
    }

    @GetMapping("/me-by-token")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirements(value = {})
    @Operation(
            summary = "Текущий пользователь по токену (в заголовке token)",
            description = "Вставьте JWT в поле 'token'. Можно с префиксом 'Bearer ' или без.",
            parameters = @Parameter(name = "token", in = ParameterIn.HEADER, required = true,
                    example = "eyJhbGciOiJIUzI1NiJ9...")
    )
    public UserFullInfoResponseDto meByToken(@RequestHeader String token
    ) {
        return userQueryService.getUserFullInfoByToken(token);
    }
}
