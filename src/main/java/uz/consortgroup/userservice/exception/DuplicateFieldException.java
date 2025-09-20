package uz.consortgroup.userservice.exception;

import lombok.Getter;

@Getter
public class DuplicateFieldException extends RuntimeException {
    private final String fieldName;
    private final String fieldValue;

    public DuplicateFieldException(String fieldName, String fieldValue, String message) {
        super(message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}