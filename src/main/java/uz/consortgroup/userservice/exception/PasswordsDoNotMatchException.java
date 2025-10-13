package uz.consortgroup.userservice.exception;

public class PasswordsDoNotMatchException extends RuntimeException {
    public PasswordsDoNotMatchException(String message) {
        super(message);
    }
}
