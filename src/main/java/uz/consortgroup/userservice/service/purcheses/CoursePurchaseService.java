package uz.consortgroup.userservice.service.purcheses;

import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;

import java.util.List;

public interface CoursePurchaseService {
    void saveAllPurchasedCourses(List<CoursePurchasedEvent> coursePurchasedEvents);
}
