package uz.consortgroup.userservice.exception;

public class CourseCreationRollbackException extends RuntimeException {
    public CourseCreationRollbackException(String message,  Throwable cause) {
        super(message, cause);
    }
}
