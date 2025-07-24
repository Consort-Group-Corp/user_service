package uz.consortgroup.userservice.service.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.event.course_group.CourseForumGroupCreatedEvent;
import uz.consortgroup.userservice.service.forum_group.CourseForumGroupCreationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourseForumGroupCreationProcessorTest {

    @Mock
    private CourseForumGroupCreationService creationService;

    @InjectMocks
    private CourseForumGroupCreationProcessor processor;

    @Test
    void process_Success() {
        CourseForumGroupCreatedEvent event = new CourseForumGroupCreatedEvent();
        List<CourseForumGroupCreatedEvent> events = List.of(event);

        processor.process(events);

        verify(creationService).saveAllForumGroupCreations(events);
    }

    @Test
    void process_EmptyList() {
        processor.process(List.of());
        verify(creationService).saveAllForumGroupCreations(List.of());
    }

    @Test
    void process_ServiceThrowsException() {
        List<CourseForumGroupCreatedEvent> events = List.of(new CourseForumGroupCreatedEvent());
        doThrow(new RuntimeException("Test error")).when(creationService).saveAllForumGroupCreations(events);

        assertThrows(RuntimeException.class, () -> processor.process(events));
    }
}