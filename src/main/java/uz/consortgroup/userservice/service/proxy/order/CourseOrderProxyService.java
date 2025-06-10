package uz.consortgroup.userservice.service.proxy.order;

import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;

public interface CourseOrderProxyService {
    OrderResponse createCourseOrder(OrderRequest orderRequest);
}
