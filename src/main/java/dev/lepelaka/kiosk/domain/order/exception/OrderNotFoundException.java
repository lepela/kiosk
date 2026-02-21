package dev.lepelaka.kiosk.domain.order.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class OrderNotFoundException extends OrderException {
    private final Long orderId;
    private final String orderNumber;

    public OrderNotFoundException(Long orderId) {
        super(ErrorCode.ORDER_NOT_FOUND);
        this.orderId = orderId;
        this.orderNumber = null;
    }

    public OrderNotFoundException(String orderNumber) {
        super(ErrorCode.ORDER_NOT_FOUND);
        this.orderId = null;
        this.orderNumber = orderNumber;
    }

    @Override
    public String getMessage() {
        return String.format("%s (주문 ID : %d, 주문번호 : %s)", super.getMessage(), orderId, orderNumber);
    }
}
