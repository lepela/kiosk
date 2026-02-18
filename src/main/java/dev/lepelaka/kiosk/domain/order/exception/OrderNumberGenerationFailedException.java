package dev.lepelaka.kiosk.domain.order.exception;

public class OrderNumberGenerationFailedException extends OrderException {

    public OrderNumberGenerationFailedException() {
        super(OrderErrorCode.ORDER_NUMBER_GENERATION_FAILED);
    }

    // 원인 예외(DataAccessException 등)를 함께 로깅하기 위한 생성자
    public OrderNumberGenerationFailedException(Throwable cause) {
        super(OrderErrorCode.ORDER_NUMBER_GENERATION_FAILED);
        initCause(cause);
    }
}