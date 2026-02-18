package dev.lepelaka.kiosk.domain.order.dto;

import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.order.entity.OrderItem;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(
        @NotEmpty
        List<OrderItemRequest> orderItems,
        @NotNull
        Long terminalId
) {

}
