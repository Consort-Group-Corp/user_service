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
import uz.consortgroup.core.api.v1.dto.forum.CreateForumGroupByHrRequest;
import uz.consortgroup.core.api.v1.dto.forum.HrForumGroupCreateResponse;
import uz.consortgroup.userservice.handler.ErrorResponse;
import uz.consortgroup.userservice.service.forum_group.HrForumGroupService;

@RestController
@RequestMapping("/api/v1/hr")
@RequiredArgsConstructor
@Validated
@Tag(name = "HR Forum Groups", description = "Создание форумных групп HR-ом")
@SecurityRequirement(name = "bearerAuth")
public class HrController {

    private final HrForumGroupService hrForumGroupService;

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            operationId = "createHrForumGroup",
            summary = "Создать форум-группу",
            description = "Создаёт форумную группу для заданного курса и набора пользователей. " +
                    "Время указывать в формате ISO-8601 (UTC), например: `2025-08-14T09:00:00Z`."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Группа создана",
                    content = @Content(
                            schema = @Schema(implementation = HrForumGroupCreateResponse.class),
                            examples = @ExampleObject(value = """
                {
                  "groupId": "8b2f9b6d-0b3a-4c68-9d5e-1a2b3c4d5e6f"
                }
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные (валидация/диапазон дат)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль HR/ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Курс или пользователь не найдены",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт (например, уже есть активная группа на этот период)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public HrForumGroupCreateResponse createForumGroupByHr(
            @RequestBody(
                    description = "Параметры создаваемой группы",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateForumGroupByHrRequest.class),
                            examples = @ExampleObject(value = """
                {
                  "courseId": "b6a1dd6c-f6f1-4b42-9b0a-2d7a2d2d3c1f",
                  "userIds": [
                    "2fbbf276-e14f-4db3-a2b3-db543d51d69c",
                    "6a9d5d01-1f2a-4eab-a4f3-0a1b2c3d4e5f"
                  ],
                  "startTime": "2025-08-14T09:00:00Z",
                  "endTime":   "2025-09-14T09:00:00Z"
                }
                """)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid CreateForumGroupByHrRequest request
    ) {
        return hrForumGroupService.createHrForumGroup(request);
    }
}
