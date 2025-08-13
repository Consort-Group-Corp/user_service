package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.userservice.handler.ErrorResponse;
import uz.consortgroup.userservice.service.proxy.order.CourseOrderProxyService;

@RestController
@RequestMapping("/api/v1/users/course-orders")
@RequiredArgsConstructor
@Tag(name = "Course Orders", description = "Оформление заказов на курсы")
@SecurityRequirement(name = "bearerAuth")
public class CourseOrderController {

    private final CourseOrderProxyService courseOrderProxyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            operationId = "createCourseOrder",
            summary = "Создать заказ на курс",
            description = "Создаёт заказ на оплату курса. `externalOrderId` — внешний ID заказа; сумма — в UZS (целое число)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Заказ создан",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class),
                            examples = @ExampleObject(value = """
                {
                  "id": "5fc0a7c1-6f07-47a9-9a45-9a0b5f7e3c2a",
                  "userId": "2fbbf276-e14f-4db3-a2b3-db543d51d69c",
                  "externalOrderId": "ORD-2025-000123",
                  "itemId": "b6a1dd6c-f6f1-4b42-9b0a-2d7a2d2d3c1f",
                  "amount": 150000,
                  "itemType": "COURSE",
                  "source": "PAYME",
                  "status": "NEW",
                  "createdAt": "2025-08-13T12:34:56Z",
                  "updatedAt": "2025-08-13T12:34:56Z"
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                {
                  "timestamp": "2025-08-13T19:54:12",
                  "status": 400,
                  "error": "Validation failed",
                  "message": "amount must be at least 500"
                }
                """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не авторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ресурс не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт (например, externalOrderId уже существует)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public OrderResponse createOrder(
            @RequestBody(
                    description = "Данные заказа",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OrderRequest.class))
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid OrderRequest request
    ) {
        return courseOrderProxyService.createCourseOrder(request);
    }
}
