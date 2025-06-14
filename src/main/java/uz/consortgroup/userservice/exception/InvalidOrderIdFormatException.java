package uz.consortgroup.userservice.exception;

public class InvalidOrderIdFormatException extends RuntimeException {
    public InvalidOrderIdFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
