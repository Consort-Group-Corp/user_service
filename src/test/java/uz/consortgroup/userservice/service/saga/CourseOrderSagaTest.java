package uz.consortgroup.userservice.service.saga;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.course.response.course.CoursePurchaseValidationResponseDto;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.client.OrderFeignClient;
import uz.consortgroup.userservice.exception.CourseNotPurchasableException;
import uz.consortgroup.userservice.exception.OrderCreationRollbackException;
import uz.consortgroup.userservice.validator.CourseAccessValidator;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseOrderSagaTest {

    @Mock
    private OrderFeignClient orderFeignClient;

    @Mock
    private CourseFeignClient courseFeignClient;

    @Mock
    private CourseAccessValidator courseAccessValidator;

    @InjectMocks
    private CourseOrderSaga courseOrderSaga;

    @Test
    void run_ShouldSuccessfullyCreateOrder() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String externalOrderId = "ext-123";

        OrderRequest request = new OrderRequest();
        request.setItemId(courseId);
        request.setUserId(userId);
        request.setExternalOrderId(externalOrderId);

        CoursePurchaseValidationResponseDto validationResponse = new CoursePurchaseValidationResponseDto();
        validationResponse.setPurchasable(true);

        OrderResponse expectedResponse = new OrderResponse();

        when(courseFeignClient.validateCourseForPurchase(courseId)).thenReturn(validationResponse);
        doNothing().when(courseAccessValidator).validateUserCanPurchaseCourse(userId, courseId);
        when(orderFeignClient.createOrder(request)).thenReturn(expectedResponse);

        OrderResponse actualResponse = courseOrderSaga.run(request);

        assertSame(expectedResponse, actualResponse);
        verify(courseFeignClient).validateCourseForPurchase(courseId);
        verify(courseAccessValidator).validateUserCanPurchaseCourse(userId, courseId);
        verify(orderFeignClient).createOrder(request);
    }

    @Test
    void run_ShouldThrowCourseNotPurchasableException() {
        UUID courseId = UUID.randomUUID();
        OrderRequest request = new OrderRequest();
        request.setItemId(courseId);

        CoursePurchaseValidationResponseDto validationResponse = new CoursePurchaseValidationResponseDto();
        validationResponse.setPurchasable(false);

        when(courseFeignClient.validateCourseForPurchase(courseId)).thenReturn(validationResponse);

        assertThrows(CourseNotPurchasableException.class, () -> courseOrderSaga.run(request));
    }


    @Test
    void run_ShouldRollbackOrderOnGeneralException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String externalOrderId = "ext-123";

        OrderRequest request = new OrderRequest();
        request.setItemId(courseId);
        request.setUserId(userId);
        request.setExternalOrderId(externalOrderId);

        CoursePurchaseValidationResponseDto validationResponse = new CoursePurchaseValidationResponseDto();
        validationResponse.setPurchasable(true);

        RuntimeException exception = new RuntimeException("Some error");

        when(courseFeignClient.validateCourseForPurchase(courseId)).thenReturn(validationResponse);
        doNothing().when(courseAccessValidator).validateUserCanPurchaseCourse(userId, courseId);
        when(orderFeignClient.createOrder(request)).thenThrow(exception);
        doNothing().when(orderFeignClient).deleteOrder(externalOrderId);

        assertThrows(OrderCreationRollbackException.class, () -> courseOrderSaga.run(request));
        verify(orderFeignClient).deleteOrder(externalOrderId);
    }

    @Test
    void run_ShouldThrowOrderCreationRollbackExceptionWhenRollbackFails() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String externalOrderId = "ext-123";

        OrderRequest request = new OrderRequest();
        request.setItemId(courseId);
        request.setUserId(userId);
        request.setExternalOrderId(externalOrderId);

        CoursePurchaseValidationResponseDto validationResponse = new CoursePurchaseValidationResponseDto();
        validationResponse.setPurchasable(true);

        RuntimeException exception = new RuntimeException("Some error");
        RuntimeException rollbackException = new RuntimeException("Rollback failed");

        when(courseFeignClient.validateCourseForPurchase(courseId)).thenReturn(validationResponse);
        doNothing().when(courseAccessValidator).validateUserCanPurchaseCourse(userId, courseId);
        when(orderFeignClient.createOrder(request)).thenThrow(exception);
        doThrow(rollbackException).when(orderFeignClient).deleteOrder(externalOrderId);

        OrderCreationRollbackException thrown = assertThrows(
                OrderCreationRollbackException.class,
                () -> courseOrderSaga.run(request)
        );

        assertEquals("Ошибка при откате заказа", thrown.getMessage());
        assertSame(rollbackException, thrown.getCause());
    }
}