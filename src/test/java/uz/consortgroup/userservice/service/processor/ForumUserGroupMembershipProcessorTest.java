package uz.consortgroup.userservice.service.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;
import uz.consortgroup.userservice.service.forum_group.ForumUserGroupMembershipService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ForumUserGroupMembershipProcessorTest {

    @Mock
    private ForumUserGroupMembershipService membershipService;

    @InjectMocks
    private ForumUserGroupMembershipProcessor processor;

    @Test
    void process_Success() {
        CoursePurchasedEvent event = new CoursePurchasedEvent();
        List<CoursePurchasedEvent> events = List.of(event);

        processor.process(events);

        verify(membershipService).assignUsersToForumGroups(events);
    }

    @Test
    void process_EmptyList() {
        processor.process(List.of());
        verify(membershipService).assignUsersToForumGroups(List.of());
    }

    @Test
    void process_ServiceThrowsException() {
        List<CoursePurchasedEvent> events = List.of(new CoursePurchasedEvent());
        doThrow(new RuntimeException("Test error")).when(membershipService).assignUsersToForumGroups(events);

        assertThrows(RuntimeException.class, () -> processor.process(events));
    }
}