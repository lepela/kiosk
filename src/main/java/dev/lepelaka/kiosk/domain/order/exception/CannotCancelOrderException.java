package dev.lepelaka.kiosk.domain.order.exception;

import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;

public class CannotCancelOrderException extends OrderException {
    private final Long orderId;
    private final OrderStatus status;

    public CannotCancelOrderException(Long orderId, OrderStatus status) {
        super(OrderErrorCode.CANNOT_CANCEL_ORDER);
        this.orderId = orderId;
        this.status = status;
    }

    public String getMessage() {
        return String.format("%s 주문 취소 불가(주문 ID : %d, 주문상태 : %s)", super.getMessage(), orderId, status);
    }
}
