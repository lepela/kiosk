package dev.lepelaka.kiosk.domain.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderItemRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderResponse;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.domain.order.service.OrderService;
import dev.lepelaka.kiosk.global.common.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @DisplayName("주문을 생성한다.")
    @Test
    void createOrder() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(List.of(new OrderItemRequest(100L, 2)), 1L);
        Long orderId = 1L;
        OrderResponse response = createOrderResponse(orderId, "20231010-0001");

        given(orderService.createOrder(any(OrderCreateRequest.class))).willReturn(orderId);
        given(orderService.getOrder(orderId)).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/order/1"))
                .andExpect(jsonPath("$.orderNumber").value("20231010-0001"));
    }

    @DisplayName("주문 번호로 주문을 조회한다.")
    @Test
    void getOrder() throws Exception {
        // given
        String orderNumber = "20231010-0001";
        OrderResponse response = createOrderResponse(1L, orderNumber);

        given(orderService.getOrder(orderNumber)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/order/{orderNumber}", orderNumber))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(orderNumber));
    }

    @DisplayName("주문을 취소한다.")
    @Test
    void cancelOrder() throws Exception {
        // given
        Long orderId = 1L;

        // when & then
        mockMvc.perform(post("/api/v1/order/{orderId}/cancel", orderId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(orderService).cancelOrder(orderId);
    }

    @DisplayName("관리자용 주문 목록을 조회한다.")
    @Test
    void getAllOrders() throws Exception {
        // given
        OrderResponse response = createOrderResponse(1L, "20231010-0001");
        PageResponse<OrderResponse> pageResponse = PageResponse.from(new PageImpl<>(List.of(response)));

        given(orderService.getAllOrders(any(Pageable.class))).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/v1/order/admin")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("20231010-0001"));
    }

    @DisplayName("특정 상태의 주문 목록을 조회한다.")
    @Test
    void getOrdersByStatus() throws Exception {
        // given
        OrderStatus status = OrderStatus.PENDING;
        OrderResponse response = createOrderResponse(1L, "20231010-0001");
        PageResponse<OrderResponse> pageResponse = PageResponse.from(new PageImpl<>(List.of(response)));

        given(orderService.getOrdersByStatus(eq(status), any(Pageable.class))).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/v1/order/admin")
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("20231010-0001"));
    }

    @DisplayName("주문을 확정(결제 완료)한다.")
    @Test
    void confirmOrder() throws Exception {
        Long orderId = 1L;
        mockMvc.perform(post("/api/v1/order/{orderId}/confirm", orderId))
                .andExpect(status().isOk());
        verify(orderService).confirmOrder(orderId);
    }

    @DisplayName("주문을 완료(제조 완료)한다.")
    @Test
    void completeOrder() throws Exception {
        Long orderId = 1L;
        mockMvc.perform(post("/api/v1/order/{orderId}/complete", orderId))
                .andExpect(status().isOk());
        verify(orderService).completeOrder(orderId);
    }

    private OrderResponse createOrderResponse(Long id, String orderNumber) {
        return OrderResponse.builder().id(id).orderNumber(orderNumber).totalAmount(10000).status(OrderStatus.PENDING).build();
    }
}