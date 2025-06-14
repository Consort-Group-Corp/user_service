package uz.consortgroup.userservice.service.purcheses;

import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;

import java.util.List;
import java.util.UUID;

public interface CoursePurchaseService {
    void saveAllPurchasedCourses(List<CoursePurchasedEvent> coursePurchasedEvents);
    boolean hasActiveAccess(UUID userId, UUID courseId);
}
