package dev.lepelaka.kiosk.domain.order.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class OrderNumberGenerationFailedException extends OrderException {

    public OrderNumberGenerationFailedException() {
        super(ErrorCode.ORDER_NUMBER_GENERATION_FAILED);
    }

    public OrderNumberGenerationFailedException(Throwable cause) {
        super(ErrorCode.ORDER_NUMBER_GENERATION_FAILED);
        initCause(cause);
    }
}
