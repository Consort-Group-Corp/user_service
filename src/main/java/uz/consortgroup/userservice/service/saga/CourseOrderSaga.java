package uz.consortgroup.userservice.service.saga;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.course.response.course.CoursePurchaseValidationResponseDto;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.client.OrderFeignClient;
import uz.consortgroup.userservice.exception.CourseNotPurchasableException;
import uz.consortgroup.userservice.exception.OrderAlreadyExistsException;
import uz.consortgroup.userservice.exception.OrderCreationRollbackException;
import uz.consortgroup.userservice.validator.CourseAccessValidator;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseOrderSaga {
    private final OrderFeignClient orderFeignClient;
    private final CourseFeignClient courseFeignClient;
    private final CourseAccessValidator courseAccessValidator;

    @AllAspect
    public OrderResponse run(OrderRequest orderRequest) {
        UUID courseId = orderRequest.getItemId();

        CoursePurchaseValidationResponseDto course = courseFeignClient.validateCourseForPurchase(courseId);
        if (!course.isPurchasable()) {
            throw new CourseNotPurchasableException("Невозможно купить курс: курс не найден, либо недоступен для покупки");
        }

        try {
            courseAccessValidator.validateUserCanPurchaseCourse(orderRequest.getUserId(), courseId);
            return orderFeignClient.createOrder(orderRequest);

        } catch (FeignException.BadRequest e) {
            String message = e.contentUTF8();
            if (message != null && message.contains("order_already_exists")) {
                throw new OrderAlreadyExistsException("Заказ уже существует", e);
            }

            rollbackOrder(orderRequest.getExternalOrderId(), e);
        } catch (Exception ex) {
            rollbackOrder(orderRequest.getExternalOrderId(), ex);
        }

        throw new IllegalStateException("Не удалось создать заказ");
    }

    private void rollbackOrder(String externalOrderId, Exception cause) {
        try {
            orderFeignClient.deleteOrder(externalOrderId);
        } catch (Exception rollbackEx) {
            throw new OrderCreationRollbackException("Ошибка при откате заказа", rollbackEx);
        }
        throw new OrderCreationRollbackException("Ошибка при создании заказа", cause);
    }
}
