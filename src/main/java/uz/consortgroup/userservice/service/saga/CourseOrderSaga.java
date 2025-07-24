package uz.consortgroup.userservice.service.saga;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.course.response.course.CoursePurchaseValidationResponseDto;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.client.OrderFeignClient;
import uz.consortgroup.userservice.exception.CourseNotPurchasableException;
import uz.consortgroup.userservice.exception.OrderAlreadyExistsException;
import uz.consortgroup.userservice.exception.OrderCreationRollbackException;
import uz.consortgroup.userservice.validator.CourseAccessValidator;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseOrderSaga {
    private final OrderFeignClient orderFeignClient;
    private final CourseFeignClient courseFeignClient;
    private final CourseAccessValidator courseAccessValidator;

    public OrderResponse run(OrderRequest orderRequest) {
        UUID courseId = orderRequest.getItemId();
        UUID userId = orderRequest.getUserId();
        String externalOrderId = orderRequest.getExternalOrderId();

        log.info("Starting course order saga: courseId={}, userId={}, externalOrderId={}", courseId, userId, externalOrderId);

        CoursePurchaseValidationResponseDto course = courseFeignClient.validateCourseForPurchase(courseId);
        log.debug("Course purchasability check: courseId={}, isPurchasable={}", courseId, course.isPurchasable());

        if (!course.isPurchasable()) {
            log.warn("Course is not purchasable: courseId={}", courseId);
            throw new CourseNotPurchasableException("Невозможно купить курс: курс не найден, либо недоступен для покупки");
        }

        try {
            courseAccessValidator.validateUserCanPurchaseCourse(userId, courseId);
            OrderResponse orderResponse = orderFeignClient.createOrder(orderRequest);
            log.info("Order created successfully: orderId={}, externalOrderId={}", orderResponse.getExternalOrderId(), externalOrderId);
            return orderResponse;

        } catch (FeignException.BadRequest e) {
            String message = e.contentUTF8();
            log.warn("BadRequest from order service: message={}", message);

            if (message != null && message.contains("order_already_exists")) {
                log.info("Order already exists: externalOrderId={}", externalOrderId);
                throw new OrderAlreadyExistsException("Заказ уже существует", e);
            }

            rollbackOrder(externalOrderId, e);
        } catch (Exception ex) {
            log.error("Unexpected error while creating order: externalOrderId={}, message={}", externalOrderId, ex.getMessage(), ex);
            rollbackOrder(externalOrderId, ex);
        }

        throw new IllegalStateException("Не удалось создать заказ");
    }

    private void rollbackOrder(String externalOrderId, Exception cause) {
        try {
            log.info("Attempting to rollback order: externalOrderId={}", externalOrderId);
            orderFeignClient.deleteOrder(externalOrderId);
            log.info("Rollback successful: externalOrderId={}", externalOrderId);
        } catch (Exception rollbackEx) {
            log.error("Rollback failed: externalOrderId={}, reason={}", externalOrderId, rollbackEx.getMessage(), rollbackEx);
            throw new OrderCreationRollbackException("Ошибка при откате заказа", rollbackEx);
        }
        log.warn("Rollback completed due to error: externalOrderId={}, cause={}", externalOrderId, cause.getMessage());
        throw new OrderCreationRollbackException("Ошибка при создании заказа", cause);
    }
}
