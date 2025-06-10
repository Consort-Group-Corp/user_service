package uz.consortgroup.userservice.exception;

public class OrderCreationRollbackException extends RuntimeException {
    public OrderCreationRollbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
