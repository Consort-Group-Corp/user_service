package uz.consortgroup.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderItemType;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderStatus;
import uz.consortgroup.userservice.service.proxy.order.CourseOrderProxyService;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CourseOrderControllerTest {

    @Mock
    private CourseOrderProxyService courseOrderProxyService;

    @InjectMocks
    private CourseOrderController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createOrder_shouldReturnCreatedResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        String externalOrderId = "order_" + System.currentTimeMillis();

        OrderRequest request = new OrderRequest();
        request.setUserId(userId);
        request.setExternalOrderId(externalOrderId);
        request.setItemId(itemId);
        request.setItemType(OrderItemType.COURSE);
        request.setAmount(1000L);
        request.setSource(OrderSource.PAYME);

        OrderResponse response = OrderResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .externalOrderId(externalOrderId)
                .itemId(itemId)
                .itemType(OrderItemType.COURSE)
                .amount(1000L)
                .source(OrderSource.PAYME)
                .status(OrderStatus.NEW)
                .createdAt(Instant.now())
                .build();

        when(courseOrderProxyService.createCourseOrder(any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/course-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").exists(),
                        jsonPath("$.userId").value(userId.toString()),
                        jsonPath("$.itemId").value(itemId.toString()),
                        jsonPath("$.amount").value(1000),
                        jsonPath("$.status").value("NEW"),
                        jsonPath("$.externalOrderId").value(externalOrderId),
                        jsonPath("$.itemType").value("COURSE"),
                        jsonPath("$.source").value("PAYME")
                );
    }

    @Test
    void createOrder_shouldReturn400WhenInvalidCourseId() throws Exception {
        String invalidRequest = "{\"courseId\":\"invalid-uuid\",\"amount\":100}";

        mockMvc.perform(post("/api/v1/users/course-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_shouldReturn400WhenNegativeAmount() throws Exception {
        UUID courseId = UUID.randomUUID();
        String invalidRequest = "{\"courseId\":\"" + courseId + "\",\"amount\":-100}";

        mockMvc.perform(post("/api/v1/users/course-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_shouldReturn400WhenMissingCourseId() throws Exception {
        String invalidRequest = "{\"amount\":100}";

        mockMvc.perform(post("/api/v1/users/course-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_shouldReturn400WhenMissingAmount() throws Exception {
        UUID courseId = UUID.randomUUID();
        String invalidRequest = "{\"courseId\":\"" + courseId + "\"}";

        mockMvc.perform(post("/api/v1/users/course-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_shouldReturnCorrectContentType() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(UUID.randomUUID());
        request.setExternalOrderId("order_" + System.currentTimeMillis());
        request.setItemId(UUID.randomUUID());
        request.setItemType(OrderItemType.COURSE);
        request.setAmount(1000L);
        request.setSource(OrderSource.PAYME);

        when(courseOrderProxyService.createCourseOrder(any(OrderRequest.class)))
                .thenReturn(new OrderResponse());

        mockMvc.perform(post("/api/v1/users/course-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isCreated(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                );
    }
}