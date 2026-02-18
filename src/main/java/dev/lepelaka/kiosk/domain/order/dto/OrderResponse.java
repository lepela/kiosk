package dev.lepelaka.kiosk.domain.order.dto;

import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private int totalAmount;
    private OrderStatus status;
    private Long terminalId;
    private List<OrderItemResponse> orderItems;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .terminalId(order.getTerminal().getId())
                .orderItems(order.getOrderItems().stream().map(OrderItemResponse::from).collect(Collectors.toList()))
                .build();
    }
}
