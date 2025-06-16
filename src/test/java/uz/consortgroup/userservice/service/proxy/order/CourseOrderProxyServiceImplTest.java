package uz.consortgroup.userservice.service.proxy.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.userservice.service.saga.CourseOrderSaga;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseOrderProxyServiceImplTest {

    @Mock
    private CourseOrderSaga courseOrderSaga;

    @InjectMocks
    private CourseOrderProxyServiceImpl courseOrderProxyService;

    @Test
    void createCourseOrder_Success() {
        OrderRequest request = new OrderRequest();
        OrderResponse expectedResponse = new OrderResponse();
        
        when(courseOrderSaga.run(request)).thenReturn(expectedResponse);
        
        OrderResponse actualResponse = courseOrderProxyService.createCourseOrder(request);
        
        assertEquals(expectedResponse, actualResponse);
        verify(courseOrderSaga).run(request);
    }

    @Test
    void createCourseOrder_NullRequest_NoException() {
        assertDoesNotThrow(() -> courseOrderProxyService.createCourseOrder(null));
    }


    @Test
    void createCourseOrder_SagaThrowsException() {
        OrderRequest request = new OrderRequest();
        
        when(courseOrderSaga.run(request))
            .thenThrow(new RuntimeException("Saga error"));
        
        assertThrows(RuntimeException.class, () -> 
            courseOrderProxyService.createCourseOrder(request));
    }
}