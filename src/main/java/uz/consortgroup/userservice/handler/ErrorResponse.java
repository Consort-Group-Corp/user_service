package uz.consortgroup.userservice.handler;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "ErrorResponse", description = "Стандартная структура ошибки API")
public record ErrorResponse(

        @Schema(description = "Время возникновения ошибки",
                type = "string", format = "date-time",
                example = "2025-08-13T19:54:12")
        LocalDateTime timestamp,

        @Schema(description = "HTTP статус", example = "400")
        int status,

        @Schema(description = "Краткое описание ошибки", example = "Validation failed")
        String error,

        @Schema(description = "Детали/сообщение", example = "Invalid format date")
        String message
) {
    public ErrorResponse(int status, String error, String message) {
        this(LocalDateTime.now(), status, error, message);
    }
}
