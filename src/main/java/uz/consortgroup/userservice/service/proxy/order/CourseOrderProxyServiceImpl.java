package uz.consortgroup.userservice.service.proxy.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.service.saga.CourseOrderSaga;

@Service
@RequiredArgsConstructor
public class CourseOrderProxyServiceImpl implements CourseOrderProxyService {
    private final CourseOrderSaga courseOrderSaga;

    @Override
    @AllAspect
    public OrderResponse createCourseOrder(OrderRequest orderRequest) {
        return courseOrderSaga.run(orderRequest);
    }
}
