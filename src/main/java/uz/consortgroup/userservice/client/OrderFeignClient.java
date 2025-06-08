package uz.consortgroup.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.userservice.config.client.FeignClientConfig;

@FeignClient(
        name = "payment-service",
        contextId = "orderClient",
        url = "${payment.service.url}",
        configuration = FeignClientConfig.class
)
public interface OrderFeignClient {
    @PostMapping("/api/v1/orders")
    OrderResponse createOrder(@RequestBody OrderRequest orderRequest);

    @DeleteMapping("/api/v1/orders/{externalOrderId}")
    void deleteOrder(@PathVariable("externalOrderId") String externalOrderId);
}
