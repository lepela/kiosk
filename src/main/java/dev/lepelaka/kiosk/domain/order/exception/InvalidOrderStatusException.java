package dev.lepelaka.kiosk.domain.order.exception;

import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class InvalidOrderStatusException extends OrderException {
    private final OrderStatus status;
    private final OrderStatus newStatus;

    public InvalidOrderStatusException(OrderStatus status, OrderStatus newStatus) {
        super(ErrorCode.INVALID_ORDER_STATUS);
        this.status = status;
        this.newStatus = newStatus;
    }

    @Override
    public String getMessage() {
        return String.format("%s (주문상태 %s 는 %s 로 전환될수 없습니다)", super.getMessage(), status, newStatus);
    }
}
