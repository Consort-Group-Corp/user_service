package uz.consortgroup.userservice.exception;

public class CourseAlreadyPurchasedAndStillActiveException extends RuntimeException {
    public CourseAlreadyPurchasedAndStillActiveException(String message) {
        super(message);
    }
}
