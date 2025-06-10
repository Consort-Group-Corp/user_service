package uz.consortgroup.userservice.service.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;
import uz.consortgroup.userservice.service.purcheses.CoursePurchaseService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoursePurchasedEventProcessor implements ActionProcessor<CoursePurchasedEvent>{
    private final CoursePurchaseService coursePurchaseService;

    @Override
    public void process(List<CoursePurchasedEvent> events) {
       coursePurchaseService.saveAllPurchasedCourses(events);
    }
}
