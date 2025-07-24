package uz.consortgroup.userservice.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.exception.AuthenticationFailedException;
import uz.consortgroup.userservice.exception.CourseAlreadyPurchasedAndStillActiveException;
import uz.consortgroup.userservice.exception.CourseNotPurchasableException;
import uz.consortgroup.userservice.exception.InvalidOrderIdFormatException;
import uz.consortgroup.userservice.exception.InvalidPasswordException;
import uz.consortgroup.userservice.exception.InvalidTokenException;
import uz.consortgroup.userservice.exception.InvalidVerificationCodeException;
import uz.consortgroup.userservice.exception.OrderAlreadyExistsException;
import uz.consortgroup.userservice.exception.OrderCreationRollbackException;
import uz.consortgroup.userservice.exception.PasswordMismatchException;
import uz.consortgroup.userservice.exception.UserAlreadyExistsException;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.exception.UserRoleNotFoundException;
import uz.consortgroup.userservice.exception.VerificationCodeExpiredException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // General Exception Handlers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", "Произошла непредвиденная ошибка"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.error("IllegalStateException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Illegal state", ex.getMessage()));
    }

    // Validation Handlers
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        log.error("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", String.join("; ", errors)));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", ex.getMessage()));
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", ex.getMessage()));
    }

    // User-related Exception Handlers
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("UserNotFoundException: ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found", "User with provided ID was not found"));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.error("UserAlreadyExistsException: ", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), "User already exists", ex.getMessage()));
    }

    // Verification Code Exception Handlers
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationCodeException(InvalidVerificationCodeException ex) {
        log.error("InvalidVerificationCodeException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid verification code", ex.getMessage()));
    }

    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<ErrorResponse> handleVerificationCodeExpiredException(VerificationCodeExpiredException ex) {
        log.error("VerificationCodeExpiredException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Verification code expired", ex.getMessage()));
    }

    // Role-related Exception Handlers
    @ExceptionHandler(UserRoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserRoleNotFoundException(UserRoleNotFoundException ex) {
        log.error("UserRoleNotFoundException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid role", ex.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException ex) {
        log.error("InvalidPasswordException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid password", ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        log.error("InvalidTokenException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid token", ex.getMessage()));
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatchException(PasswordMismatchException ex) {
        log.error("PasswordMismatchException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Password mismatch", ex.getMessage()));
    }

    @ExceptionHandler(OrderAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleOrderAlreadyExistsException(OrderAlreadyExistsException ex) {
        log.error("OrderAlreadyExistsException: ", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), "Order already exists", ex.getMessage()));
    }

    @ExceptionHandler(OrderCreationRollbackException.class)
    public ResponseEntity<ErrorResponse> handleOrderCreationRollbackException(OrderCreationRollbackException ex) {
        log.error("OrderCreationRollbackException: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Order creation rollback", ex.getMessage()));
    }

    @ExceptionHandler(CourseNotPurchasableException.class)
    public ResponseEntity<ErrorResponse> handleCourseNotPurchasableException(CourseNotPurchasableException ex) {
        log.error("CourseNotPurchasableException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Course not purchasable", ex.getMessage()));
    }

    @ExceptionHandler(InvalidOrderIdFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderIdFormatException(InvalidOrderIdFormatException ex) {
        log.error("InvalidOrderIdFormatException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid order ID format", ex.getMessage()));
    }

    @ExceptionHandler(CourseAlreadyPurchasedAndStillActiveException.class)
    public ResponseEntity<ErrorResponse> handleCourseAlreadyPurchasedAndStillActiveException(CourseAlreadyPurchasedAndStillActiveException ex) {
        log.error("CourseAlreadyPurchasedAndStillActiveException: ", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), "Course already purchased and still active", ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailedException(AuthenticationFailedException ex) {
        log.error("AuthenticationFailedException: ", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Authentication failed", ex.getMessage()));
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleFeign404(FeignException.NotFound ex) {
        String body = ex.contentUTF8();

        if (body.contains("Course with id")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            404,
                            "Курс не найден",
                            "Курс не существует или недоступен для покупки"
                    ));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        404,
                        "Ресурс не найден",
                        "Сторонний сервис вернул 404"
                ));
    }


    // JSON and Data Integrity Handlers
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("JSON parse error: ", ex);
        if (ex.getMessage() != null && ex.getMessage().contains("not one of the values accepted for Enum class")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "Invalid role value",
                            "The provided role is not valid. Valid roles are: " + Arrays.toString(UserRole.values())
                    ));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid JSON format",
                        "The request contains invalid JSON"
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Database integrity violation: {}", ex.getMessage());
        if (ex.getMessage().contains("verification_codes_user_id_fkey")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid user ID", "User with the given ID does not exist"));
        }
        if (ex.getMessage().contains("unique_constraint")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Data conflict", "Duplicate data found or constraint violation"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid data", "Database integrity violation occurred"));
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormatException(InvalidFormatException ex) {
        if (ex.getTargetType() != null && ex.getTargetType().isEnum()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "Invalid role value",
                            "Valid roles are: " + Arrays.toString(UserRole.values())
                    ));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid format",
                        ex.getMessage())
                );
    }
}
