package dev.lepelaka.kiosk.domain.order.exception;

import dev.lepelaka.kiosk.global.exception.BusinessException;
import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class OrderException extends BusinessException {
    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected OrderException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
