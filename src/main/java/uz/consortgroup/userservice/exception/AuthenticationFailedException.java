package uz.consortgroup.userservice.exception;

public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String email, Throwable cause) {
        super("Authentication failed for user: " + email, cause);
    }
}
