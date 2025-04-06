package uz.consortgroup.user_service.exception;

public class UserRoleNotFoundException extends RuntimeException {
    public UserRoleNotFoundException(String message) {
        super(message);
    }
}