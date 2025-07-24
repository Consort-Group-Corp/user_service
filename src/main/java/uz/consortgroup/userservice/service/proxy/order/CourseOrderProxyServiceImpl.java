package uz.consortgroup.userservice.service.proxy.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.userservice.service.saga.CourseOrderSaga;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseOrderProxyServiceImpl implements CourseOrderProxyService {
    private final CourseOrderSaga courseOrderSaga;

    @Override
    public OrderResponse createCourseOrder(OrderRequest orderRequest) {
        log.info("Starting course order creation: userId={}, itemId={}, itemType={}, externalOrderId={}, amount={}, source={}",
                orderRequest.getUserId(),
                orderRequest.getItemId(),
                orderRequest.getItemType(),
                orderRequest.getExternalOrderId(),
                orderRequest.getAmount(),
                orderRequest.getSource()
        );
        try {
            OrderResponse response = courseOrderSaga.run(orderRequest);
            log.debug("Course order created successfully. OrderId: {}", response.getExternalOrderId());
            return response;
        } catch (Exception e) {
            log.error("Failed to create course order for externalOrderId={}, userId={}, itemId={}",
                    orderRequest.getExternalOrderId(),
                    orderRequest.getUserId(),
                    orderRequest.getItemId(),
                    e
            );
            throw e;
        }
    }
}
