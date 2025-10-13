package uz.consortgroup.userservice.exception;

public class InvalidOrExpiredResetTokenException extends RuntimeException {
    public InvalidOrExpiredResetTokenException(String message) {
        super(message);
    }
}
