package uz.consortgroup.userservice.entity.enumeration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserRoleEnumValidator implements ConstraintValidator<ValidUserRoleEnum, Enum<?>> {

    private Enum<?>[] enumValues;

    @Override
    public void initialize(ValidUserRoleEnum constraintAnnotation) {
        this.enumValues = constraintAnnotation.enumClass().getEnumConstants();
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        for (Enum<?> enumValue : enumValues) {
            if (enumValue == value) {
                return true;
            }
        }
        return false;
    }
}