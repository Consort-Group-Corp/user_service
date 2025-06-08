package uz.consortgroup.userservice.service.saga;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.client.OrderFeignClient;
import uz.consortgroup.userservice.exception.OrderAlreadyExistsException;
import uz.consortgroup.userservice.exception.OrderCreationRollbackException;

@Service
@RequiredArgsConstructor
public class CourseOrderSaga {
    private final OrderFeignClient orderFeignClient;

    @AllAspect
    public OrderResponse run(OrderRequest orderRequest) {
        try {
            return orderFeignClient.createOrder(orderRequest);

        } catch (FeignException.BadRequest e) {
            String message = e.contentUTF8();
            if (message != null && message.contains("order_already_exists")) {
                throw new OrderAlreadyExistsException("Заказ уже существует", e);
            }

            try {
                orderFeignClient.deleteOrder(orderRequest.getExternalOrderId());
            } catch (Exception rollbackEx) {
                throw new OrderCreationRollbackException("Ошибка при откате заказа", rollbackEx);
            }

            throw new OrderCreationRollbackException("Ошибка при создании заказа", e);

        } catch (Exception ex) {
            try {
                orderFeignClient.deleteOrder(orderRequest.getExternalOrderId());
            } catch (Exception rollbackEx) {
                throw new OrderCreationRollbackException("Ошибка при откате заказа", rollbackEx);
            }

            throw new OrderCreationRollbackException("Ошибка при создании заказа", ex);
        }
    }

}
