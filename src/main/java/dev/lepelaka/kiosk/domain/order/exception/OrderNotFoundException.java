package dev.lepelaka.kiosk.domain.order.exception;

public class OrderNotFoundException extends OrderException {
    private Long orderId;
    private String orderNumber;
    public OrderNotFoundException(Long orderId) {
        super(OrderErrorCode.ORDER_NOT_FOUND);
        this.orderId = orderId;
    }

    public OrderNotFoundException(String orderNumber) {
        super(OrderErrorCode.ORDER_NOT_FOUND);
        this.orderNumber = orderNumber;
    }

    public String getMessage() {
        return String.format("%s (주문 ID : %d, 주문번호 : %s)", super.getMessage(), orderId, orderNumber);
    }


}
