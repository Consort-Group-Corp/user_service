package uz.consortgroup.userservice.entity.enumeration;

import jakarta.validation.Constraint;
import org.springframework.messaging.handler.annotation.Payload;

@Constraint(validatedBy = UserRoleEnumValidator.class)
public @interface ValidUserRoleEnum {
    String message() default "Invalid value for enum";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends Enum<?>> enumClass();
}
