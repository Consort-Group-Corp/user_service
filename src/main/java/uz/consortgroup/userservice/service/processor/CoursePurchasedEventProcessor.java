package uz.consortgroup.userservice.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;
import uz.consortgroup.userservice.service.purchases.CoursePurchaseService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoursePurchasedEventProcessor implements ActionProcessor<CoursePurchasedEvent> {

    private final CoursePurchaseService coursePurchaseService;

    @Override
    public void process(List<CoursePurchasedEvent> events) {
        log.info("Processing {} CoursePurchasedEvent(s)", events.size());
        try {
            coursePurchaseService.saveAllPurchasedCourses(events);
            log.debug("Successfully processed {} CoursePurchasedEvent(s)", events.size());
        } catch (Exception e) {
            log.error("Failed to process CoursePurchasedEvents", e);
            throw e;
        }
    }
}
