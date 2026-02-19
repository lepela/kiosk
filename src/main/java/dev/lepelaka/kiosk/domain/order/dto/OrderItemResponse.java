package dev.lepelaka.kiosk.domain.order.dto;

import dev.lepelaka.kiosk.domain.order.entity.OrderItem;
import lombok.Builder;

@Builder
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private int price;

    public static OrderItemResponse from(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }
}
